package edu.example.test.dao;

import edu.example.test.entities.JpaEntity;

import java.util.Collection;
import java.util.List;

public interface JpaDao<T extends JpaEntity, ID> {

    T find(ID id);

    Collection<T> findAll();

    T save(T entity);

    List<T> saveAll(List<T> entities);

    Long count();

    void delete(T entity);

    void clear();
}
