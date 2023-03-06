package edu.example.test.persistence.concurrency.isolation;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.util.EntityManagerHelper;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.IntStream;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Set the DB isolation level on javax.persistence.jdbc.url by adding the following
 * query parameter ?sessionVariables=transaction_isolation='REPEATABLE-READS'
 */
public class RepeatableReadsIsolationTest extends IsolationLevelBaseTest {

//    @Test
    public void shouldDirtyReadNotHappen() {
        // given
        EntityManager alice = EntityManagerHelper.getNewInstance();
        EntityManager bob = EntityManagerHelper.getNewInstance();

        // when
        Dummy aliceDummy = alice.find(Dummy.class, victimId);
        aliceDummy.setValue("Changed by Alice");
        alice.flush();

        Dummy bobDummy = bob.find(Dummy.class, victimId);

        EntityManagerHelper.rollbackAndClose(alice);
        EntityManagerHelper.commitAndClose(bob);

        // then
        doInJPA(entityManagerFactorySupplier, entityManager -> {
            Dummy dummy = entityManager.find(Dummy.class, victimId);
            assertEquals(dummy.getValue(), "Default");
            assertEquals(bobDummy.getValue(), "Default");
        });
    }

//    @Test
    public void shouldNonRepeatableReadHappen() {
        // given
        doInJPA(entityManagerFactorySupplier, alice -> {
            Dummy aliceDummy = alice.find(Dummy.class, victimId);
            alice.clear();
            assertEquals(1, aliceDummy.getCountDown());

            /*
             *  if(alice.getCountDown() > 0) {
             *      // Do something
             *  }
             */

            // when
            doInJPA(entityManagerFactorySupplier, bob -> {
                Dummy bobDummy = bob.find(Dummy.class, victimId);
                bobDummy.setCountDown(bobDummy.getCountDown() - 1);
            });

            // then
            aliceDummy = alice.find(Dummy.class, victimId);
            assertEquals(1, aliceDummy.getCountDown());
        });
    }

//    @Test
    public void shouldPhantomReadNotHappen() {
        // given
        int countDownCondition = 10;
        doInJPA(entityManagerFactorySupplier, entityManager -> {
            IntStream.rangeClosed(1, 3).mapToObj(v -> new Dummy(countDownCondition)).forEach(
                    dummy -> entityManager.persist(dummy)
            );
        });

        // when
        doInJPA(entityManagerFactorySupplier, bob -> {
            List bobDummies = bob.createQuery("SELECT dummy FROM Dummy dummy WHERE dummy.countDown = :countDown")
                    .setParameter("countDown", countDownCondition)
                    .getResultList();
            assertEquals(3, bobDummies.size());
            bob.clear();

            /*
             * Do some business logic on this list which follows the condition
             */

            doInJPA(entityManagerFactorySupplier, alice -> {
                alice.persist(new Dummy(countDownCondition));
                alice.persist(new Dummy(countDownCondition));
            });

            // then
            bobDummies = bob.createQuery("SELECT dummy FROM Dummy dummy WHERE dummy.countDown = :countDown")
                    .setParameter("countDown", countDownCondition)
                    .getResultList();
            assertEquals(3, bobDummies.size());
        });
    }

}