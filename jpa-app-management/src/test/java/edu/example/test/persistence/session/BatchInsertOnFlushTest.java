package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.AbstractTest;
import org.hibernate.FlushMode;
import org.junit.jupiter.api.Test;

import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BatchInsertOnFlushTest extends AbstractTest {

    @Test
    public void shouldSaveBatches() {
        doInHibernate(sessionFactorySupplier, session -> {
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
        });
    }
}
