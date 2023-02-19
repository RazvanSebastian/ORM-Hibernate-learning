package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import org.hibernate.TransientObjectException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extra info besides tests:
 * - Hibernate only method
 * - Bypass the dirty checking mechanism
 * - Lazy update
 * - Void return: acts upon passed object
 * - Throws exception on TRANSIENT objects
 */
public class UpdateMethodTest extends AbstractMethodsTest {

    /**
     * Case: DETACHED -> PERSISTENT
     * Behaviour:
     * - Adds passed object into persistent context
     * - Triggers an update on COMMIT or FLUSH
     */
    @Test
    public void givenDetachedObject_whenUpdateIsCalled_thenNewCopyIsCreatedAndPersistedAndUpdateIsCalled() {
        // given
        final Dummy dummy = new Dummy();
        session.persist(dummy);
        final Long firstPersistId = dummy.getId();

        session.flush();
        assertEquals(1, count(session));

        session.evict(dummy);
        assertFalse(session.contains(dummy));

        // when
        dummy.setValue("Updated");
        session.update(dummy);
        session.flush();

        // then
        assertTrue(firstPersistId == dummy.getId());
        assertTrue(session.contains(dummy));
        assertEquals(1, count(session));

        session.getTransaction().commit();
    }

    /**
     * Case: TRANSIENT -> PERSISTENT
     * Behaviour:
     * - throws TransientObjectException
     */
    @Test
    public void givenTransientObject_whenUpdateIsCalled_thenThrowException() {
        // given
        final Dummy dummy = new Dummy();
        dummy.setValue("Updated");

        // when + then
        try {
            session.update(dummy);
            session.flush();
            session.getTransaction().commit();
        } catch (RuntimeException e) {
            session.getTransaction().rollback();
            assertTrue(e instanceof TransientObjectException);
        }
    }

    /**
     * Case: Update on persisted object without actual property changes.
     * Behaviour:
     * - Update SQL statement is called anyway
     * Improvements:
     * - Add @{@link org.hibernate.annotations.SelectBeforeUpdate} on entity forcing dirty checking
     */
    @Test
    public void givenPersistedObject_whenUpdateIsCalledAndNoPropertyUpdates_thenBypassDirtyChecking() {
        // given
        final Dummy dummy = new Dummy();
        dummy.setValue("Some value");
        session.persist(dummy);
        session.flush();
        session.evict(dummy);

        // when
        session.update(dummy);
        session.flush();
    }
}
