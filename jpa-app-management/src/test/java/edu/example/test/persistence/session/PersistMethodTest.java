package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.AbstractTest;
import org.junit.jupiter.api.Test;

import javax.persistence.PersistenceException;

import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Properties:
 * 1. Changes state: Transient -> Persistent
 * 2. Cascade all relations with cascade = PERSISTS or cascade = ALL
 * 3. Returns void -> mutate the passed object
 * 4. LAZY INSERT: query will be triggered only on COMMIT or FLUSH
 * 5. On PERSISTED entities NOTHING happens
 * 6. On DETACHED entities THROWS PersistenceException
 */
public class PersistMethodTest extends AbstractTest {

    @Test
    public void givenTransientObject_whenPersistIsCalled_thenAddToPersistenceContextAndStoreIntoDataBaseOnlyOnFlush() {
        doInHibernate(sessionFactorySupplier, session -> {
            // given
            final Dummy dummy = new Dummy();
            assertFalse(session.contains(dummy));

            // when
            session.persist(dummy);

            // then
            assertNotNull(dummy.getId());
            assertTrue(session.contains(dummy));

            doInHibernate(sessionFactorySupplier, countSession -> {
                // Here is a nested different transaction which doesn't know about the outer transaction since
                // the outer transactions didn't commit
                assertEquals(0, count(countSession));
            });

            assertEquals(1, count(session)); // will flush and automatically triggers an INSERT
        });
    }

    @Test
    public void givenPersistedEntity_whenPersistIsCalled_thenNothingHappens() {
        doInHibernate(sessionFactorySupplier, session -> {
            // given
            final Dummy dummy = new Dummy();
            session.persist(dummy);
            final Long idFromFirstPersist = dummy.getId().longValue();
            session.flush();

            // when
            session.persist(dummy);
            final Long idFromSecondPersist = dummy.getId().longValue();
            session.flush();

            // then
            assertEquals(idFromFirstPersist, idFromSecondPersist);
        });
    }

    @Test
    public void givenDetachedEntity_whenPersistIsCalled_thenThrowsPersistenceException() {
        doInHibernate(sessionFactorySupplier, session -> {
            // given
            final Dummy dummy = new Dummy();
            session.persist(dummy);
            session.evict(dummy);

            assertNotNull(dummy.getId());
            assertFalse(session.contains(dummy));

            // when + then
            try {
                session.persist(dummy);
            } catch (RuntimeException e) {
                assertTrue(e instanceof PersistenceException);
            }
        });
    }
}
