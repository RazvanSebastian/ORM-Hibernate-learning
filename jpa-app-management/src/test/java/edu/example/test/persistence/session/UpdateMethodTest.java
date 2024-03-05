package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.AbstractTest;
import org.hibernate.TransientObjectException;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Extra info besides tests:
 * - Hibernate only method
 * - Bypass the dirty checking mechanism
 * - Lazy update
 * - Void return: acts upon passed object
 * - Throws exception on TRANSIENT objects
 */
public class UpdateMethodTest extends AbstractTest {

    @Test
    void shouldUpdate() {
        AtomicReference<Long> id = new AtomicReference<>();

        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            Dummy dummy = new Dummy();
            dummy.setValue("Initial value");

            entityManager.persist(dummy);

            id.set(dummy.getId());
        });

        // Update entity with only one query => 1 SQL: UPDATE
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            int result = entityManager.createQuery("UPDATE Dummy d SET d.value = :newValue where d.id = :id")
                    .setParameter("newValue", "Updated value")
                    .setParameter("id", id.get())
                    .executeUpdate();
            assertEquals(1, result);

            result = entityManager.createQuery("UPDATE Dummy d SET d.value = :newValue where d.id = :id")
                    .setParameter("newValue", "Updated value again")
                    .setParameter("id", 10L)
                    .executeUpdate();
            assertEquals(0, result);
        });

        // Update entity by retrieving => 2 SQLs: SELECT + UPDATE
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            Dummy dummy = entityManager.find(Dummy.class, id.get());
            dummy.setValue("Update value with 2 SQLs");
        });
    }

    /**
     * Case: DETACHED -> PERSISTENT
     * Behaviour:
     * - Adds passed object into persistent context
     * - Triggers an update on COMMIT or FLUSH
     */
    @Test
    public void givenDetachedObject_whenUpdateIsCalled_thenNewCopyIsCreatedAndPersistedAndUpdateIsCalled() {
        // given
        doInHibernate(sessionFactorySupplier, session -> {

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
        });
    }

    /**
     * Case: TRANSIENT -> PERSISTENT
     * Behaviour:
     * - throws TransientObjectException
     */
    @Test
    public void givenTransientObject_whenUpdateIsCalled_thenThrowException() {
        // given
        doInHibernate(sessionFactorySupplier, session -> {
            final Dummy dummy = new Dummy();
            dummy.setValue("Updated");

            // when + then
            try {
                session.update(dummy);
                session.flush();
            } catch (RuntimeException e) {
                assertTrue(e instanceof TransientObjectException);
            }
        });
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
        doInHibernate(sessionFactorySupplier, session -> {
            final Dummy dummy = new Dummy();
            dummy.setValue("Some value");
            session.persist(dummy);
            session.flush();
            session.evict(dummy);

            // when
            session.update(dummy);
            session.flush();
        });
    }
}
