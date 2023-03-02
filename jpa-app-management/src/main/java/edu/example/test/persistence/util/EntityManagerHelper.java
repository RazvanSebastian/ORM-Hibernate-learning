package edu.example.test.persistence.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class EntityManagerHelper {

    public static EntityManagerFactory entityManagerFactory;

    static {
        entityManagerFactory = Persistence.createEntityManagerFactory("POSTGRESQL");
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
