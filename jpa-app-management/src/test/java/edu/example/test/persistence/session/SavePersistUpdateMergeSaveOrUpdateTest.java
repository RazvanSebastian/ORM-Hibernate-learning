package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.util.SessionHelper;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.PersistenceException;

public class SavePersistUpdateMergeSaveOrUpdateTest {

    Session session;

    @BeforeEach
    public void beforeEach() {
        session = SessionHelper.getSession();
        session.getTransaction().begin();
        session.createQuery("DELETE FROM Dummy").executeUpdate();
        session.flush();
    }

    @AfterEach
    public void afterEach() {
        session.close();
    }

    @Test
    public void persistTest() {
        Dummy dummy = new Dummy();
        session.setHibernateFlushMode(FlushMode.MANUAL);

        // Stored into persistent context: first level of cache and has assigned an id
        session.persist(dummy);
        Assertions.assertNotNull(dummy.getId());
        Assertions.assertEquals(count(session), 0);

        session.flush();
        Assertions.assertEquals(count(session), 1);

        session.getTransaction().commit();
    }

    @Test
    public void persistEvictTest() {
        Dummy dummy = new Dummy();

        session.persist(dummy);
        Assertions.assertNotNull(dummy.getId());

        session.evict(dummy);
        try {
            // Throws PersistException
            session.persist(dummy);
        } catch (RuntimeException e) {
            Assertions.assertTrue(e instanceof PersistenceException);
        }
    }

    @Test
    public void saveEvictTest() {
        Dummy dummy = new Dummy();

        session.save(dummy);
        Long id1 = dummy.getId();

        session.evict(dummy);
        session.save(dummy);
        // Should add into persistent context with different ID
        Long id2 = dummy.getId();

        Assertions.assertNotEquals(id1, id2);
        session.getTransaction().commit();
    }

    @Test
    public void mergeTest() {
        Dummy dummy = new Dummy();

        session.save(dummy);
        Long idAfterSave = dummy.getId();
        Assertions.assertEquals(count(session), 1);

        // Changes on detached objects are not persisted nor flushed into DB
        session.evict(dummy);
        // The value still exists in DB
        Assertions.assertEquals(count(session), 1);
        // But not present into persistent context
        Assertions.assertFalse(session.contains(dummy));

        // Changes on re-attached objects are updated on both persistence context and flushed to DB
        dummy.setValue("Re-attached");
        session.merge(dummy);
        Assertions.assertEquals(dummy.getId(), idAfterSave);
        Assertions.assertEquals(select(session, dummy.getId()).getValue(), "Re-attached");

        session.getTransaction().commit();
    }

    @Test
    public void updateTest() {
        Dummy dummy = new Dummy();

        session.save(dummy);
        Long idAfterSave = dummy.getId();
        Assertions.assertEquals(count(session), 1);

        session.evict(dummy);
        // The value still exists in DB
        Assertions.assertEquals(count(session), 1);
        // But not present into persistent context
        Assertions.assertFalse(session.contains(dummy));

        // Changes on objects from update method call are updated on both persistence context and flushed to DB
        dummy.setValue("Re-attached and update");
        session.update(dummy);
        // After update on evicted entity, it should generate new id
        Assertions.assertEquals(dummy.getId(), idAfterSave);
        Assertions.assertEquals(select(session, idAfterSave).getValue(), "Re-attached and update");

        session.getTransaction().commit();
    }

    private Long count(Session session) {
        return (Long) session.createQuery("SELECT COUNT(*) FROM Dummy").getSingleResult();
    }

    private Dummy select(Session session, Long id) {
        return session.get(Dummy.class, id);
    }
}
