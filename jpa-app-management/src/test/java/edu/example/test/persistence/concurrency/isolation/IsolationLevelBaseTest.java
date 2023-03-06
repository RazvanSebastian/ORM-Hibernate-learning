package edu.example.test.persistence.concurrency.isolation;

import edu.example.test.entities.Dummy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import javax.persistence.EntityManagerFactory;
import java.util.function.Supplier;

import static edu.example.test.persistence.util.EntityManagerHelper.entityManagerFactory;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

public class IsolationLevelBaseTest {

    protected static Supplier<EntityManagerFactory> entityManagerFactorySupplier;
    protected Long victimId;

    @BeforeAll
    protected static void beforeAll() {
        entityManagerFactorySupplier = () -> entityManagerFactory;
    }

    @BeforeEach
    protected void beforeEach() {
        doInJPA(entityManagerFactorySupplier, entityManager -> {
            entityManager.createQuery("DELETE FROM Dummy").executeUpdate();
            Dummy dummy = new Dummy();
            dummy.setValue("Default");
            dummy.setCountDown(1);
            entityManager.persist(dummy);
            victimId = dummy.getId();
        });
    }

}
