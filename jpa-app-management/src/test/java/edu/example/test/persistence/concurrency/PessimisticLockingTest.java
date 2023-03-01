package edu.example.test.persistence.concurrency;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.OptimisticLockException;
import javax.persistence.PessimisticLockException;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.Assert.assertTrue;

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
     * 1. T1 and T2 starts by receiving the entity (T1_version = 0; T2_version = 0)
     * 2. T2 (Long run transaction) do some updates and flush BUT doesn't commit the transaction (T1_version = 1; T2_version = 0)
     * 3. T1 do some updates
     * 4. T1 tries to flush the changes => Timeout exception while waiting for locking the row => PessimisticLockException
     * <p>
     * This is happening when one we have one long run transaction which keeps the lock and do not release it and another transactions
     * tries to acquire the lock in order to do changes but falls into Timeout exception.
     */
    @Test
    public void givenTwoParallelTransactions_whenOneTransactionIsLongRunAndTheLockWasNotReleased_thenTimeoutAndPessimisticLockExceptionIsThrown() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager1 -> {
            Dummy dummyT1 = entityManager1.find(Dummy.class, id);
            doInJPA(entityManagerFactorySupplierSupplier, entityManager2 -> {
                Dummy dummyT2 = entityManager2.find(Dummy.class, id);
                dummyT2.setValue("T2 value");
                entityManager2.flush();
//                assertEquals(1, dummyT2.getVersion());

                // T2 doesn't release the lock and T1 tries to acquire the lock => Timeout exception and PessimisticLockException
                dummyT1.setValue("T1 value");
                try {
                    entityManager1.flush();
                } catch (RuntimeException e) {
                    // Timeout trying to lock table {0}; SQL statement
                    assertTrue(e instanceof PessimisticLockException);
                }
            });
        });
    }
}
