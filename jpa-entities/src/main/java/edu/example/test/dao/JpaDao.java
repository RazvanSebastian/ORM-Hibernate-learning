package edu.example.test.dao;

import edu.example.test.entities.JpaEntity;

import java.util.Collection;

public interface JpaDao<T extends JpaEntity, ID> {

    T find(ID id);

    Collection<T> findAll();

    T save(T entity);

    void delete(T entity);

    void clear();
}
