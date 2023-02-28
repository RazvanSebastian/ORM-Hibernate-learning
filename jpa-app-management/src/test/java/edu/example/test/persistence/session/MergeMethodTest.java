package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.util.SessionHelper;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import javax.persistence.PessimisticLockException;

import static org.junit.jupiter.api.Assertions.*;

/**
 *  Extra info besides tests:
 * - JPA specification method
 * - DO NOT Bypass the dirty checking mechanism
 * - Lazy update
 * - Returns a new copy of the passes object
 * - Cascades on all relations with cascade=MERGE or cascade=ALL
 * - On PERSISTENT objects, it has no effects.
 * - Preventing lost updates (see third test)
 */
public class MergeMethodTest extends AbstractMethodsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeMethodTest.class);

    /**
     * Case: DETACHED -> PERSISTENT
     * Behaviour:
     * - Copy the detached object into a new object which is persisted
     * - Triggers an update on COMMIT or FLUSH
     */
    @Test
    public void givenDetachedObject_whenMergeIsCalled_thenNewCopyIsCreatedAndPersistedAndUpdateIsCalled() {
        // given
        final Dummy dummy = new Dummy();
        session.persist(dummy);

        session.flush();
        assertEquals(1, count(session));

        session.evict(dummy);
        assertFalse(session.contains(dummy));

        // when
        dummy.setValue("Updated");
        final Dummy mergedDummy = (Dummy) session.merge(dummy);

        // then
        assertTrue(dummy != mergedDummy);
        assertEquals(dummy.getId(), mergedDummy.getId());
        assertTrue(session.contains(mergedDummy));
        assertFalse(session.contains(dummy));
        assertEquals(1, count(session));

        session.getTransaction().commit();
    }

    /**
     * Case: TRANSIENT -> PERSISTENT
     * Behaviour:
     * - Copy the TRANSIENT object into a new object which is persisted
     * - Triggers an update on COMMIT or FLUSH
     */
    @Test
    public void givenTransientObject_whenMergeIsCalled_thenNewCopyIsCreatedAndPersistedAndUpdateIsCalled() {
        // given
        final Dummy dummy = new Dummy();
        dummy.setValue("Updated");

        // when
        final Dummy mergedDummy = (Dummy) session.merge(dummy);

        // then
        assertTrue(dummy != mergedDummy);
        assertTrue(session.contains(mergedDummy));
        assertEquals(1, count(session));

        session.getTransaction().commit();
    }

    /**
     * Long run conversation test case
     *
     *  Best practice for merge: use @Version on entities.
     *  - Updates on PERSISTED object increments the entity version.
     *  - Merge on DETACHED object DOES NOT increment the entity version.
     *
     *  When trying to merge the detached entity, Hibernate first loads the current database snapshot and
     *  attaches the loading-time state into the current Persistence Context.
     *  When copying the detached entity state onto the newly loaded entity,
     *  Hibernate detects that the version has changed,
     *  hence it throws the PessimisticLockException
     */
    @Test
    public void givenLongRunConversion_whenMergeIsCalled_thenOptimisticLockingShouldWork() {
        // given
        final Dummy detachedDummy = new Dummy();
        detachedDummy.setValue("Some value");
        session.persist(detachedDummy);
        session.flush();
        session.clear();
        session.getTransaction().commit();

        final Session thirdSession = SessionHelper.getSession();
        thirdSession.getTransaction().begin();

        final Session secondSession = SessionHelper.getSession();
        secondSession.getTransaction().begin();

        // when + then
        LOGGER.info(() -> "Second session: starts doing changes on saved object and increments version");
        final Dummy secondSessionDummy = secondSession.find(Dummy.class, detachedDummy.getId());
        secondSessionDummy.setValue("Second session changed value");
        assertEquals(0, secondSessionDummy.getVersion());
        secondSession.flush();
        assertEquals(1, secondSessionDummy.getVersion());
        LOGGER.info(() -> "Second session: flushed changed");

        try {
            LOGGER.info(() -> "Third session: starts doing changes on the same row");
            final Dummy thirdSessionDummy = thirdSession.find(Dummy.class, detachedDummy.getId());
            thirdSessionDummy.setValue("Third session changed value");
            assertEquals(0, thirdSessionDummy.getVersion());
            LOGGER.info(() -> "Third session: flush the changes on the same row and exception is thown");
            thirdSession.flush();
        } catch (RuntimeException e) {
            assertTrue(e instanceof PessimisticLockException);
        } finally {
            secondSession.getTransaction().commit();
            thirdSession.getTransaction().commit();
        }
    }
}
