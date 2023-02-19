package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.util.SessionHelper;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DifferentSessionsEqualityTest {

    @BeforeAll
    public static void beforeAll() {
        final Session session = SessionHelper.getSession();
        session.getTransaction().begin();
        session.createQuery("DELETE FROM Dummy").executeUpdate();
        session.getTransaction().commit();
        session.close();
    }

    @Test
    public void givenStoredObjectIntoDB_whenGetIsCalledFromSameSession_thenReturnSameInstanceForEntity() {
        // given
        final Session session = SessionHelper.getSession();
        session.beginTransaction();

        Dummy dummy = new Dummy();
        dummy.setValue("test");

        session.save(dummy);
        session.getTransaction().commit();
        session.close();

        // when
        final Session secondSession = SessionHelper.getSession();
        secondSession.beginTransaction();

        Dummy entity1 = secondSession.get(Dummy.class, dummy.getId());
        Dummy entity2 = secondSession.get(Dummy.class, dummy.getId());

        // then
        assertTrue(entity1 == entity2);
        secondSession.close();
    }

    @Test
    public void givenStoredObjectIntoDB_whenGetIsCalledOnDifferentSessions_thenReturnDifferentInstancesButObjectsShouldBeEquals() {
        // given
        final Session firstSession = SessionHelper.getSession();
        final Session secondSession = SessionHelper.getSession();

        final Session saveSession = SessionHelper.getSession();
        saveSession.beginTransaction();

        Dummy dummy = new Dummy();
        dummy.setValue("test");

        saveSession.save(dummy);
        saveSession.getTransaction().commit();
        saveSession.close();


        // when
        Dummy firstSessionEntity = firstSession.get(Dummy.class, dummy.getId());
        Dummy secondSessionEntity = secondSession.get(Dummy.class, dummy.getId());

        // then
        assertTrue(firstSessionEntity != secondSessionEntity);
        assertEquals(firstSessionEntity, secondSessionEntity);

        firstSession.close();
        secondSession.close();
    }
}
