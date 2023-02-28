package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.AbstractTest;
import org.junit.jupiter.api.Test;

import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;
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
public class SaveMethodTest extends AbstractTest {

    @Test
    public void givenTransientObject_whenSaveIsCalled_thenAddToPersistenceContextAndStoreIntoDataBaseOnlyOnFlush() {
        doInHibernate(sessionFactorySupplier, session -> {
            // given
            final Dummy dummy = new Dummy();
            assertFalse(session.contains(dummy));

            // when
            final Long generatedIdOnSave = (Long) session.save(dummy);

            // then
            assertTrue(session.contains(dummy));
            assertEquals(generatedIdOnSave, dummy.getId());

            doInHibernate(sessionFactorySupplier, countSession -> {
                assertEquals(0, count(countSession));
            });

            assertEquals(1, count(session)); // will flush and INSERT is triggered
        });
    }

    @Test
    public void givenPersistedEntity_whenSaveIsCalled_thenNothingHappens() {
        doInHibernate(sessionFactorySupplier, session -> {
            // given
            final Dummy dummy = new Dummy();
            session.persist(dummy);
            final Long idFromFirstPersist = dummy.getId().longValue();

            // when
            session.save(dummy);
            final Long idFromSecondPersist = dummy.getId().longValue();

            // then
            assertEquals(idFromFirstPersist, idFromSecondPersist);
        });
    }

    @Test
    public void givenDetachedEntity_whenSaveIsCalled_thenGenerateDuplicatedRecordsAfterFlush() {
        doInHibernate(sessionFactorySupplier, session -> {
            // given
            final Dummy dummy = new Dummy();
            final Long idFromFirstSave = (Long) session.save(dummy);

            session.evict(dummy); // detach

            // when
            final Long idFromSecondSave = (Long) session.save(dummy);

            // then
            assertNotEquals(idFromFirstSave, idFromSecondSave);
            session.flush();
            assertEquals(2, count(session));
        });
    }
}