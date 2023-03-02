package edu.example.test.persistence.concurrency;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.AbstractTest;
import edu.example.test.persistence.util.EntityManagerHelper;
import org.hibernate.PessimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;
import java.util.Map;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 * - Using database locks
 * - Shared (read) locks: allows readings by any transactions and writings by only the transaction which acquired the lock
 * - Exclusive (write) locks : allows readings and writings only by the transaction which acquired the lock
 * <p>
 * Deadlock:
 * - Use case: when one transaction acquire a shared lock and another transactions tries to do changes on the locked row
 * - Solution: use PESSIMISTIC_WRITE when the transaction has to do updates; if the row was locked then LockTimeoutException is thrown.
 */
public class PessimisticLockingTest extends AbstractTest {

    private long id;

    @BeforeEach
    public void beforeEach() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            entityManager.createQuery("DELETE FROM Dummy").executeUpdate();
            Dummy dummy = new Dummy();
            dummy.setCountDown(1);
            entityManager.persist(dummy);

            entityManager.flush();
            id = dummy.getId();
        });
    }

    /**
     * Test case:
     * - T1 locks with shared (read) lock
     * - T2 allowed to read the locked row by T1
     * <p>
     * Behaviour:
     * - Only T1 is allowed to do changes; the other transactions are allowed only to read.
     */
    @Test
    public void testSharedLockHappyFlow() {
        // when
        doInJPA(entityManagerFactorySupplierSupplier, entityManager1 -> {
            Dummy dummyT1 = entityManager1.find(Dummy.class, id, LockModeType.PESSIMISTIC_READ);
            doInJPA(entityManagerFactorySupplierSupplier, entityManager2 -> {
                Dummy dummyT2 = entityManager2.find(Dummy.class, id);
                assertEquals(dummyT2.getId(), dummyT1.getId());
            });
            dummyT1.setValue("Changed by T1");
        });

        // then
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            Dummy dummy = entityManager.find(Dummy.class, id);
            assertEquals("Changed by T1", dummy.getValue());
        });
    }

    /**
     * Test case:
     * - T1 acquire shared (read) lock
     * - T2 tries to acquire exclusive lock
     * <p>
     * Behaviour:
     * - The row already has a lock
     * - Another transaction can not acquire another lock until T1 release the lock
     * - LockTimeoutException is thrown
     */
    @Test
    public void testSharedLockTimeoutException() {
        // when
        doInJPA(entityManagerFactorySupplierSupplier, entityManager1 -> {
            Dummy dummyT1 = entityManager1.find(Dummy.class, id, LockModeType.PESSIMISTIC_READ);
            doInJPA(entityManagerFactorySupplierSupplier, entityManager2 -> {
                try {
                    entityManager2.find(Dummy.class, id, LockModeType.PESSIMISTIC_WRITE, Map.ofEntries(Map.entry("javax.persistence.lock.timeout", 0)));
                } catch (RuntimeException e) {
                    assertTrue(e instanceof LockTimeoutException);
                    assertTrue(e.getCause() instanceof PessimisticLockException);
                }
            });
            dummyT1.setValue("Changed by T1");
        });

        // then
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            Dummy dummy = entityManager.find(Dummy.class, id);
            assertEquals("Changed by T1", dummy.getValue());
        });
    }

    /**
     * Test case:
     * - T1 acquire exclusive (read) lock
     * - T2 tries to read the locked row
     * <p>
     * Behaviour:
     * - Only T1 is allowed to read and write
     * - When T2 tries to read LockTimeoutException is thrown
     */
    @Test
    public void testExclusiveLockTimeoutException() {
        // when
        doInJPA(entityManagerFactorySupplierSupplier, entityManager1 -> {
            Dummy dummyT1 = entityManager1.find(Dummy.class, id, LockModeType.PESSIMISTIC_WRITE);
            doInJPA(entityManagerFactorySupplierSupplier, entityManager2 -> {
                try {
                    entityManager2.find(Dummy.class, id);
                } catch (RuntimeException e) {
                    assertTrue(e instanceof LockTimeoutException);
                    assertTrue(e.getCause() instanceof PessimisticLockException);
                }
            });
            dummyT1.setValue("Changed by T1");
        });

        // then
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            Dummy dummy = entityManager.find(Dummy.class, id);
            assertEquals("Changed by T1", dummy.getValue());
        });
    }

    /**
     * Retry steps when a row is locked and another transaction tries to acquire an exclusive lock
     * 1. T1 has shared lock
     * 2. T2 tries to acquire exclusive lock
     * 3. {@link LockTimeoutException} on T2 find method
     * 4. We have to rollback T2
     * 5. Only after T1 commits or rollback the lock is released
     * 6. only new transaction can acquire an exclusive lock after the previous lock release
     */
    @Test
    public void testRetryOnAlreadySharedLockedRow() {
        // given
        EntityManager entityManager1 = EntityManagerHelper.getNewInstance();
        EntityManager entityManager2 = EntityManagerHelper.getNewInstance();

        // when
        Dummy dummyT1 = entityManager1.find(Dummy.class, id, LockModeType.PESSIMISTIC_READ);
        dummyT1.setValue("Changed by T1");
        try {
            entityManager2.find(Dummy.class, id, LockModeType.PESSIMISTIC_WRITE, Map.ofEntries(Map.entry("javax.persistence.lock.timeout", 0)));
        } catch (LockTimeoutException e) {
            // rollback initial T2
            EntityManagerHelper.rollbackAndClose(entityManager2);

            // assuming that the first transaction was commit and released the lock
            entityManager1.lock(dummyT1, LockModeType.NONE);
            EntityManagerHelper.commitAndClose(entityManager1);

            // retry
            entityManager2 = EntityManagerHelper.getNewInstance();
            Dummy dummyT2 = entityManager2.find(Dummy.class, id, LockModeType.PESSIMISTIC_WRITE, Map.ofEntries(Map.entry("javax.persistence.lock.timeout", 5000)));
            assertEquals(dummyT1.getValue(), dummyT2.getValue());

            // do change and commit
            dummyT2.setValue("Changed by T2");
            EntityManagerHelper.commitAndClose(entityManager2);
        }
    }


}
