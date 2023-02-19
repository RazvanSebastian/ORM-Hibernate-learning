package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import org.hibernate.FlushMode;
import org.junit.jupiter.api.Test;

import javax.persistence.PersistenceException;

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
public class PersistMethodTest extends AbstractMethodsTest {

    @Test
    public void givenTransientObject_whenPersistIsCalled_thenAddToPersistenceContextAndStoreIntoDataBaseOnlyOnFlush() {
        // given
        final Dummy dummy = new Dummy();
        session.setHibernateFlushMode(FlushMode.COMMIT);
        assertFalse(session.contains(dummy));

        // when
        session.persist(dummy);

        // then
        assertNotNull(dummy.getId());
        assertTrue(session.contains(dummy));
        assertEquals(0, count(session));

        session.flush();
        session.getTransaction().commit();

        assertEquals(1, count(session));
    }

    @Test
    public void givenPersistedEntity_whenPersistIsCalled_thenNothingHappens() {
        // given
        final Dummy dummy = new Dummy();
        session.persist(dummy);
        final Long idFromFirstPersist = dummy.getId().longValue();

        // when
        session.persist(dummy);
        final Long idFromSecondPersist = dummy.getId().longValue();
        session.getTransaction().commit();

        // then
        assertEquals(idFromFirstPersist, idFromSecondPersist);
    }

    @Test
    public void givenDetachedEntity_whenPersistIsCalled_thenThrowsPersistenceException() {
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
        } finally {
            session.getTransaction().commit();
        }
    }
}
