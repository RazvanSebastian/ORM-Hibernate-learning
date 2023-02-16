package edu.example.test.persistence.entity;

import edu.example.test.entities.Dummy;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.junit.jupiter.api.*;

public class DummyEntityTest {

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @BeforeAll
    static void init() {
        entityManagerFactory = Persistence.createEntityManagerFactory("H2DB");
    }

    @AfterAll
    static void clear() {
        entityManagerFactory.close();
    }

    @BeforeEach
    public void initLocal() {
        entityManager = entityManagerFactory.createEntityManager();
    }

    @AfterEach
    public void clearLocal() {
        entityManager.close();
    }

    @Test
    public void shouldPersistEntity() {
        // given
        Dummy dummyEntity = new Dummy();
        dummyEntity.setValue("value");

        // when
        entityManager.getTransaction().begin();
        entityManager.persist(dummyEntity);
        entityManager.getTransaction().commit();

        // then
        Assertions.assertNotNull(dummyEntity.getId());
    }
}
