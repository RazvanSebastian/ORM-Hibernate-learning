package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Extra info besides tests:
 * - JPA specification method
 * - DO NOT Bypass the dirty checking mechanism
 * - Lazy update
 * - Returns a new copy of the passes object
 * - Cascades on all relations with cascade=MERGE or cascade=ALL
 * - On PERSISTENT objects, it has no effects.
 * - Preventing lost updates (see third test)
 */
public class MergeMethodTest extends AbstractTest {

    /**
     * Case: DETACHED -> PERSISTENT
     * Behaviour:
     * - Copy the detached object into a new object which is persisted
     * - Triggers an update on COMMIT or FLUSH
     */
    @Test
    public void givenDetachedObject_whenMergeIsCalled_thenNewCopyIsCreatedAndPersistedAndUpdateIsCalled() {
        // given
        AtomicReference<Long> id = new AtomicReference<>();
        doInHibernate(sessionFactorySupplier, session -> {
            final Dummy dummy = new Dummy();
            session.persist(dummy);
            session.flush();
            assertEquals(1, count(session));
            id.set(dummy.getId());
        });

        doInHibernate(sessionFactorySupplier, session -> {
            final Dummy persistedDummy = session.find(Dummy.class, id.get());
            session.evict(persistedDummy);
            assertFalse(session.contains(persistedDummy));

            // when
            persistedDummy.setValue("Updated");
            final Dummy mergedDummy = (Dummy) session.merge(persistedDummy);

            // then
            assertTrue(persistedDummy != mergedDummy);
            assertEquals(persistedDummy.getId(), mergedDummy.getId());
            assertTrue(session.contains(mergedDummy));
            assertFalse(session.contains(persistedDummy));
            assertEquals(1, count(session));
        });
    }

    /**
     * Case: TRANSIENT -> PERSISTENT
     * Behaviour:
     * - Copy the TRANSIENT object into a new object which is persisted
     * - Triggers an update on COMMIT or FLUSH
     */
    @Test
    public void givenTransientObject_whenMergeIsCalled_thenNewCopyIsCreatedAndPersistedAndUpdateIsCalled() {
        doInHibernate(sessionFactorySupplier, session -> {
            // given
            final Dummy dummy = new Dummy();
            dummy.setValue("Updated");

            // when
            final Dummy mergedDummy = (Dummy) session.merge(dummy);

            // then
            assertTrue(dummy != mergedDummy);
            assertTrue(session.contains(mergedDummy));
            assertEquals(1, count(session));
        });

    }

}
