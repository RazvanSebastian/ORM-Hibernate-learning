package edu.example.test.persistence.dao;

import edu.example.test.dao.JpaDao;
import edu.example.test.entities.JpaEntity;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.PersistenceContext;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Transactional
public abstract class BaseJpaDao<T extends JpaEntity, ID> implements JpaDao<T, ID> {
    private Class<T> entityClass;

    @PersistenceContext
    private EntityManager entityManager;

    public BaseJpaDao() {
        this.entityClass = getEntityClass();
    }

    @Override
    public T find(ID id) {
        return entityManager.find(entityClass, id);
    }

    @Override
    public Collection<T> findAll() {
        return entityManager.createQuery("SELECT entity FROM " + entityClass.getSimpleName() + " entity", entityClass).getResultList();
    }

    @Override
    public T save(T entity) {
        if (getIdentifier(entity) == null) {
            entityManager.persist(entity);
        } else {
            entityManager.merge(entity);
        }
        return entity;
    }

    @Override
    public List<T> saveAll(List<T> entities) {
        for (int i = 0; i < entities.size(); i++) {
            entityManager.persist(entities.get(i));
            if (i % 20 == 0) {
                entityManager.flush();
            }
        }
        return entities;
    }

    @Override
    public Long count() {
        return (Long) entityManager.createQuery("SELECT COUNT(entity) FROM " + entityClass.getSimpleName() + " entity").getSingleResult();
    }

    @Override
    public void delete(T entity) {
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

    @Override
    public void clear() {
        final List<T> entities = entityManager.createQuery("SELECT entity FROM " + entityClass.getSimpleName() + " entity", entityClass).getResultList();
        entities.stream()
                .map(entityManager::merge)
                .forEach(entityManager::remove);
    }

    private ID getIdentifier(T entity) {
        try {
            Field identifierField = Arrays.stream(entityClass.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Id.class))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("Identifier field is missing"));
            identifierField.setAccessible(true);
            return (ID) identifierField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<T> getEntityClass() {
        final ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class<T>) type.getActualTypeArguments()[0];
    }
}
