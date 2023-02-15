package edu.example.test.persistence.dao;

import edu.example.test.persistence.entity.JpaEntity;

import java.util.Collection;

public interface JpaDao<T extends JpaEntity, ID> {

    T find(ID id);

    Collection<T> findAll();

    T save(T entity);

    void delete(T entity);

    void clear();
}
