package edu.example.test.persistence.concurrency;

import edu.example.test.entities.Dummy;
import edu.example.test.entities.lock.OptimisticDirtyDummy;
import edu.example.test.entities.lock.OptimisticVersionDummy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static edu.example.test.persistence.util.EntityManagerHelper.entityManagerFactory;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OptimisticLockingTest {
    private static Supplier<EntityManagerFactory> entityManagerFactorySupplier;

    @BeforeAll
    private static void beforeAll() {
        entityManagerFactorySupplier = () -> entityManagerFactory;
    }

    /**
     * - Starts with two parallel requests which both starts with same source of truth, by fetching a record. (countDown = 1)
     * - Request1 countDown and flush the changes before Request2. (countDown = 0)
     * - Request2 is not aware of changes made by Request1 and decrease from his source of truth where countDown = 1.
     * - In the end the update from first transaction made by Request1 will be lost and countDown will be 0 instead of -1.
     */
    @Test
    public void testLostUpdates() throws InterruptedException {
        // given
        AtomicReference<Long> id = new AtomicReference<>();
        AtomicReference<Dummy> dummyT1 = new AtomicReference<>();
        AtomicReference<Dummy> dummyT2 = new AtomicReference<>();

        doInJPA(entityManagerFactorySupplier, entityManager -> {
            Dummy dummy = new Dummy();
            dummy.setCountDown(1);
            entityManager.persist(dummy);
            id.set(dummy.getId());
        });

        // when
        CountDownLatch countDownLatch = new CountDownLatch(2);
        Runnable request1 = () -> doInJPA(entityManagerFactorySupplier, entityManager -> {
            Dummy dummy = entityManager.find(Dummy.class, id.get());
            dummy.setCountDown(dummy.getCountDown() - 1);

            dummyT1.set(dummy);
            countDownLatch.countDown();
        });
        Runnable request2 = () -> doInJPA(entityManagerFactorySupplier, entityManager -> {
            try {
                Dummy dummy = entityManager.find(Dummy.class, id.get());
                Thread.sleep(100);
                dummy.setCountDown(dummy.getCountDown() - 1);

                dummyT2.set(dummy);
                countDownLatch.countDown();
            } catch (InterruptedException e) {
            }
        });
        new Thread(request1).start();
        new Thread(request2).start();
        countDownLatch.await();

        // then
        assertEquals(0, dummyT1.get().getCountDown());
        assertEquals(0, dummyT2.get().getCountDown());
    }

    /**
     * Optimistic locking with version
     * - Version property with annotation {@link javax.persistence.Version}
     * Behaviour:
     * - If a previous transaction already changed at least one property, the version will increase in DB and
     * any other transactions which have an entity with an older version, by flush/commit changes will throw
     * {@link OptimisticLockException}
     */
    @Test
    public void testVersionOptimisticLockingError() {
        // given
        AtomicReference<Long> id = initializeForOptimisticVersionLocking();

        // when + then
        doInJPA(entityManagerFactorySupplier, entityManager1 -> {
            OptimisticVersionDummy dummyT1 = entityManager1.find(OptimisticVersionDummy.class, id.get());
            doInJPA(entityManagerFactorySupplier, entityManager2 -> {
                OptimisticVersionDummy dummyT2 = entityManager2.find(OptimisticVersionDummy.class, id.get());
                dummyT2.setCountDown(dummyT2.getCountDown() - 1);
                entityManager2.flush();
            });
            // The lock was released after T2 commit but T1 is doing updates on older version of the entity.
            dummyT1.setName("Name changed by dummy T1");
            try {
                entityManager1.flush();
            } catch (RuntimeException e) {
                assertTrue(e instanceof OptimisticLockException);
            }
        });
    }

    /**
     * Solution test for optimistic locking using {@link javax.persistence.Version}
     */
    @Test
    public void testVersionOptimisticLockingSolution() {
        // given
        AtomicReference<Long> id = initializeForOptimisticVersionLocking();

        // when
        doInJPA(entityManagerFactorySupplier, entityManager1 -> {
            OptimisticVersionDummy dummyT1 = entityManager1.find(OptimisticVersionDummy.class, id.get());
            doInJPA(entityManagerFactorySupplier, entityManager2 -> {
                OptimisticVersionDummy dummyT2 = entityManager2.find(OptimisticVersionDummy.class, id.get());
                dummyT2.setCountDown(dummyT2.getCountDown() - 1);
                entityManager2.flush();
            });
            // The lock was released after T2 commit but T1 is doing updates on older version of the entity.
            try {
                dummyT1.setCountDown(dummyT1.getCountDown() - 1);
                dummyT1.setName("Name changed by dummy T1");
                entityManager1.flush();
            } catch (RuntimeException e) {
                retryTransactionOnOptimisticVersionLock(dummyT1, 1);
            }
        });

        // then
        doInJPA(entityManagerFactorySupplier, entityManager -> {
            OptimisticVersionDummy dummy = entityManager.find(OptimisticVersionDummy.class, id.get());
            assertEquals(-1, dummy.getCountDown());
            assertEquals("Name changed by dummy T1", dummy.getName());
        });
    }

    /**
     * Optimistic locking with Dirty
     * - Apply on class level: {@link org.hibernate.annotations.OptimisticLocking} annotation
     * with {@link org.hibernate.annotations.OptimisticLockType}
     * - Apply on class level {@link org.hibernate.annotations.DynamicUpdate}
     * Behaviour:
     * - Will lock only properties which where not have been changed by a previous transaction.
     */
    @Test
    public void testOptimisticDirtyLocking() {
        // given
        AtomicReference<Long> id = new AtomicReference<>();
        doInJPA(entityManagerFactorySupplier, entityManager -> {
            OptimisticDirtyDummy optimisticDirtyDummy = new OptimisticDirtyDummy();
            optimisticDirtyDummy.setCountDown(1);
            optimisticDirtyDummy.setName("Default name");
            entityManager.persist(optimisticDirtyDummy);
            id.set(optimisticDirtyDummy.getId());
        });

        // when
        doInJPA(entityManagerFactorySupplier, entityManager1 -> {
            OptimisticDirtyDummy dummyT1 = entityManager1.find(OptimisticDirtyDummy.class, id.get());
            doInJPA(entityManagerFactorySupplier, entityManager2 -> {
                OptimisticDirtyDummy dummyT2 = entityManager2.find(OptimisticDirtyDummy.class, id.get());
                dummyT2.setCountDown(dummyT2.getCountDown() - 1);
                entityManager2.flush();
            });
            // The lock was released after T2 commit but T1 is doing updates on older version of the entity.
            dummyT1.setName("Name changed by dummy T1");
        });

        // then
        doInJPA(entityManagerFactorySupplier, entityManager -> {
            OptimisticDirtyDummy dummy = entityManager.find(OptimisticDirtyDummy.class, id.get());
            assertEquals(0, dummy.getCountDown());
            assertEquals("Name changed by dummy T1", dummy.getName());
        });
    }

    private AtomicReference<Long> initializeForOptimisticVersionLocking() {
        AtomicReference<Long> id = new AtomicReference<>();
        doInJPA(entityManagerFactorySupplier, entityManager -> {
            OptimisticVersionDummy optimisticVersionDummy = new OptimisticVersionDummy();
            optimisticVersionDummy.setCountDown(1);
            entityManager.persist(optimisticVersionDummy);
            id.set(optimisticVersionDummy.getId());
        });
        return id;
    }

    private void retryTransactionOnOptimisticVersionLock(OptimisticVersionDummy dummy, int retryTimes) {
        AtomicReference<Integer> retries = new AtomicReference<>(retryTimes);
        while (retries.get() > 0) {
            doInJPA(entityManagerFactorySupplier, entityManager -> {
                OptimisticVersionDummy d = entityManager.find(OptimisticVersionDummy.class, dummy.getId());
                d.setCountDown(d.getCountDown() - 1);
                d.setName("Name changed by dummy T1");
                try {
                    entityManager.flush();
                    retries.set(-1);
                } catch (RuntimeException e) {
                    retries.set(retries.get() - 1);
                }
            });
        }
    }
}
