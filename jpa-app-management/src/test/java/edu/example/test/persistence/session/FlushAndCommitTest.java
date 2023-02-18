package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.util.SessionHelper;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.LoggerFactory;

public class FlushAndCommitTest {

    @Test
    public void shouldSaveBatches() {
        Session session = SessionHelper.getSession();
        session.setHibernateFlushMode(FlushMode.MANUAL);
        Transaction transaction = session.beginTransaction();

        session.createQuery("DELETE FROM Dummy").executeUpdate();

        for (int i = 1; i <= 100; i++) {
            Dummy dummy = new Dummy();
            session.save(dummy);
            if (i % 20 == 0) {
                Assertions.assertTrue(count(session) == i - 20);

                session.flush();
                session.clear();

                Assertions.assertTrue(count(session) == i);
            }
        }

        transaction.commit();
        session.close();
    }

    private Long count(Session session) {
        return (Long) session.createQuery("SELECT COUNT(*) FROM Dummy").getSingleResult();
    }
}
