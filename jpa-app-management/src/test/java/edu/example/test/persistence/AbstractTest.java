package edu.example.test.persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;

import javax.persistence.EntityManagerFactory;
import java.util.function.Supplier;

import static edu.example.test.persistence.util.SessionHelper.sessionFactory;
import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;

public class AbstractTest {

    protected static Supplier<EntityManagerFactory> entityManagerFactorySupplierSupplier;
    protected static Supplier<SessionFactory> sessionFactorySupplier;

    static {
        entityManagerFactorySupplierSupplier = () -> sessionFactory.openSession().getEntityManagerFactory();
        sessionFactorySupplier = () -> sessionFactory;
    }

    protected Session session;

    @BeforeEach
    void beforeEach() {
        doInHibernate(sessionFactorySupplier, session -> {
            session.createQuery("DELETE FROM Dummy").executeUpdate();
        });
    }

    protected Long count(Session session) {
        return (Long) session.createQuery("SELECT COUNT(*) FROM Dummy").getSingleResult();
    }

}
