package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import org.hibernate.FlushMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BatchInsertOnFlushTest extends AbstractMethodsTest {

    @Test
    public void shouldSaveBatches() {
        session.setHibernateFlushMode(FlushMode.COMMIT);
        for (int i = 1; i <= 100; i++) {
            Dummy dummy = new Dummy();
            session.save(dummy);
            if (i % 20 == 0) {
                assertEquals(i - 20, count(session));

                session.flush();

                assertEquals(i, count(session));
            }
        }
        session.getTransaction().commit();
    }
}
