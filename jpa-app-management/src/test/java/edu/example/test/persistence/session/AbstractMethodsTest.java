package edu.example.test.persistence.session;

import edu.example.test.persistence.util.SessionHelper;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class AbstractMethodsTest {

    protected Session session;

    @BeforeEach
    void beforeEach() {
        session = SessionHelper.getSession();
        session.getTransaction().begin();
        session.createQuery("DELETE FROM Dummy").executeUpdate();
        session.flush();
    }

    @AfterEach
    void afterEach() {
        session.close();
    }

    protected Long count(Session session) {
        return (Long) session.createQuery("SELECT COUNT(*) FROM Dummy").getSingleResult();
    }

}
