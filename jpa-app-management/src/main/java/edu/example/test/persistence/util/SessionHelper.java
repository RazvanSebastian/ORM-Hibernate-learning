package edu.example.test.persistence.util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import javax.persistence.EntityManagerFactory;
import java.util.function.Supplier;

public final class SessionHelper {

    private static SessionFactory sessionFactory;

    static {
        StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder().configure().build();
        Metadata metadata = new MetadataSources(standardServiceRegistry).getMetadataBuilder().build();
        sessionFactory = metadata.buildSessionFactory();
    }

    public static Session getSession() {
        return sessionFactory.openSession();
    }

    public static Supplier<EntityManagerFactory> getEntityManagerFactory() {
        return () -> sessionFactory.openSession().getEntityManagerFactory();
    }

    public static void closeFactory() {
        sessionFactory.close();
    }
}
