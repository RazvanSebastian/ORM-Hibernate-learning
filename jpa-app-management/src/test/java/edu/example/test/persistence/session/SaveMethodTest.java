package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import org.hibernate.FlushMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Original Hibernate method; NOT SUPPORTED BY JPA IMPLEMENTATION
 * <p>
 * Properties:
 * 1. Changes state: Transient -> Persistent
 * 2. Cascade all relations with cascade = PERSISTS or cascade = ALL
 * 3. Returns Serializable value of ID and mutate the passed object
 * 4. LAZY INSERT: query will be triggered only on COMMIT or FLUSH
 * 5. On PERSISTED entities NOTHING happens
 * 6. On DETACHED entities ASSIGNS new id => duplicated records into DB after committing or flushing
 */
public class SaveMethodTest extends AbstractMethodsTest {

    @Test
    public void givenTransientObject_whenSaveIsCalled_thenAddToPersistenceContextAndStoreIntoDataBaseOnlyOnFlush() {
        // given
        final Dummy dummy = new Dummy();
        session.setHibernateFlushMode(FlushMode.COMMIT);
        assertFalse(session.contains(dummy));

        // when
        final Long generatedIdOnSave = (Long) session.save(dummy);

        // then
        assertTrue(session.contains(dummy));
        assertEquals(generatedIdOnSave, dummy.getId());
        assertEquals(0, count(session));

        session.flush();
        assertEquals(1, count(session));

        session.getTransaction().commit();

    }

    @Test
    public void givenPersistedEntity_whenSaveIsCalled_thenNothingHappens() {
        // given
        final Dummy dummy = new Dummy();
        session.persist(dummy);
        final Long idFromFirstPersist = dummy.getId().longValue();

        // when
        session.save(dummy);
        final Long idFromSecondPersist = dummy.getId().longValue();
        session.getTransaction().commit();

        // then
        assertEquals(idFromFirstPersist, idFromSecondPersist);
    }

    @Test
    public void givenDetachedEntity_whenSaveIsCalled_thenGenerateDuplicatedRecordsAfterFlush() {
        // given
        final Dummy dummy = new Dummy();

        final Long idFromFirstSave = (Long) session.save(dummy);

        session.evict(dummy);
        assertNotNull(dummy.getId());
        assertFalse(session.contains(dummy));

        // when
        final Long idFromSecondSave = (Long) session.save(dummy);
        assertNotEquals(idFromFirstSave, idFromSecondSave);

        session.flush();
        assertEquals(2, count(session));

        session.getTransaction().commit();
    }
}