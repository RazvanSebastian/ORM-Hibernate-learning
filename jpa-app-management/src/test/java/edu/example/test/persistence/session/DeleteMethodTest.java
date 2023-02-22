package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extra info besides tests:
 * - Hibernate only method
 * - On PERSISTED -> FLUSH -> DELETE : delete the related row and evict
 * - ON PERSISTED -> FLUSH -> EVICT -> DELETE : delete by identifier of the passed object
 * - ON TRANSIENT -> DELETE : delete statement is triggered but nothing happens
 */
public class DeleteMethodTest extends AbstractMethodsTest {

    @Test
    public void givenPersistedDummy_whenDeleteIsCalled_thenObjectIsDeleted() {
        // given
        final Dummy dummy = new Dummy();
        session.persist(dummy);
        session.flush();

        // when
        session.delete(dummy);

        // then
        assertFalse(session.contains(dummy));
        assertEquals(0, count(session));
    }

    @Test
    public void givenTransientDummyWithIdOfRelatedStoredData_whenDeleteIsCalled_thenObjectIsDeleted() {
        // given
        final Dummy dummy = new Dummy();
        session.persist(dummy);
        session.flush();
        session.clear();

        // when
        session.delete(dummy);

        // then
        assertFalse(session.contains(dummy));
        assertEquals(0, count(session));
    }

    @Test
    public void givenTransientDummyWithoutRelatedStoredData_whenDeleteIsCalled_thenDeleteStatementIsTriggered() {
        // given
        final Dummy dummy = new Dummy();

        // when
        session.delete(dummy);

        // then
        assertTrue(true);
    }
}
