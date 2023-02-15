package edu.example.test.persistence.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public final class EntityManagerHelper {

    private static EntityManagerFactory entityManagerFactory;

    static {
        entityManagerFactory = Persistence.createEntityManagerFactory("H2DB");
    }

    private EntityManagerHelper() {
    }

    public static EntityManager getNewInstance() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        return entityManager;
    }

    public static void commitAndClose(EntityManager entityManager) {
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    public static void rollbackAndClose(EntityManager entityManager) {
        entityManager.getTransaction().rollback();
        entityManager.close();
    }
}
