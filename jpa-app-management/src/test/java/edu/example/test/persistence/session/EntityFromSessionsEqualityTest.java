package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.util.SessionHelper;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class EntityFromSessionsEqualityTest {

    @BeforeAll
    public static void beforeAll() {
        Session session = SessionHelper.getSession();
        session.setHibernateFlushMode(FlushMode.MANUAL);
        session.getTransaction().begin();
        session.createQuery("DELETE FROM Dummy").executeUpdate();
        session.flush();
        session.getTransaction().commit();
        session.close();
    }

    @Test
    public void shouldBeEqualsInstancesFromSameSession() {
        final Session session = SessionHelper.getSession();
        session.beginTransaction();

        Dummy dummy = new Dummy();
        dummy.setValue("test");
        session.save(dummy);

        session.getTransaction().commit();
        session.close();

        final Session secondSession = SessionHelper.getSession();
        secondSession.beginTransaction();
        Dummy entity1 = secondSession.get(Dummy.class, dummy.getId());
        Dummy entity2 = secondSession.get(Dummy.class, dummy.getId());

        Assertions.assertTrue(entity1 == entity2);
        secondSession.close();
    }

    @Test
    public void shouldNotBeEqualsInstancesFromSameSession() {
        final Session firstSession = SessionHelper.getSession();
        final Session secondSession = SessionHelper.getSession();

        firstSession.beginTransaction();
        secondSession.beginTransaction();

        Dummy firstSessionEntity = new Dummy();
        firstSessionEntity.setValue("test");
        firstSession.save(firstSessionEntity);
        firstSession.getTransaction().commit();
        firstSessionEntity = firstSession.get(Dummy.class, firstSessionEntity.getId());

        Dummy secondSessionEntity = secondSession.get(Dummy.class, firstSessionEntity.getId());

        Assertions.assertFalse(firstSessionEntity == secondSessionEntity);
        Assertions.assertTrue(firstSessionEntity.equals(secondSessionEntity));

        firstSession.close();
        secondSession.close();
    }
}
