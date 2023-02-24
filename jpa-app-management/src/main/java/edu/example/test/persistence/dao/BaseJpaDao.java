package edu.example.test.persistence.dao;

import edu.example.test.dao.JpaDao;
import edu.example.test.entities.JpaEntity;
import edu.example.test.persistence.util.EntityManagerHelper;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.TransactionRequiredException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseJpaDao<T extends JpaEntity, ID> implements JpaDao<T, ID> {
    private static final Logger LOGGER = Logger.getLogger(BaseJpaDao.class.getName());

    private Class<T> entityClass;

    public BaseJpaDao() {
        this.entityClass = getEntityClass();
    }

    @Override
    public T find(ID id) {
        EntityManager entityManager = EntityManagerHelper.getNewInstance();
        T entity = entityManager.find(entityClass, id);
        EntityManagerHelper.commitAndClose(entityManager);
        return entity;
    }

    @Override
    public Collection<T> findAll() {
        EntityManager entityManager = EntityManagerHelper.getNewInstance();
        List<T> entities = entityManager.createQuery("SELECT entity FROM " + entityClass.getSimpleName() + " entity", entityClass).getResultList();
        EntityManagerHelper.commitAndClose(entityManager);
        return entities;
    }

    @Override
    public T save(T entity) {
        EntityManager entityManager = EntityManagerHelper.getNewInstance();
        try {
            if (getIdentifier(entity) == null) {
                entityManager.persist(entity);
            } else {
                entityManager.merge(entity);
            }
            EntityManagerHelper.commitAndClose(entityManager);
        } catch (IllegalAccessException e) {
            EntityManagerHelper.rollbackAndClose(entityManager);
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }
        return entity;
    }

    @Override
    public List<T> saveAll(List<T> entities) {
        EntityManager entityManager = EntityManagerHelper.getNewInstance();
        try {
            for (int i = 0; i < entities.size(); i++) {
                entityManager.persist(entities.get(i));
                if (i % 20 == 0) {
                    entityManager.flush();
                }
            }
            EntityManagerHelper.commitAndClose(entityManager);
            return entities;
        } catch (RuntimeException e) {
            EntityManagerHelper.rollbackAndClose(entityManager);
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long count() {
        EntityManager entityManager = EntityManagerHelper.getNewInstance();
        Long result = (Long) entityManager.createQuery("SELECT COUNT(entity) FROM " + entityClass.getSimpleName() + " entity").getSingleResult();
        EntityManagerHelper.commitAndClose(entityManager);
        return result;
    }

    @Override
    public void delete(T entity) {
        EntityManager entityManager = EntityManagerHelper.getNewInstance();
        try {
            entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
            EntityManagerHelper.commitAndClose(entityManager);
        } catch (IllegalArgumentException | TransactionRequiredException e) {
            EntityManagerHelper.rollbackAndClose(entityManager);
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {
        EntityManager entityManager = EntityManagerHelper.getNewInstance();
        try {
            final List<T> entities = entityManager.createQuery("SELECT entity FROM " + entityClass.getSimpleName() + " entity", entityClass).getResultList();
            entities.stream()
                    .map(entityManager::merge)
                    .forEach(entityManager::remove);
            EntityManagerHelper.commitAndClose(entityManager);
        } catch (IllegalArgumentException | TransactionRequiredException e) {
            EntityManagerHelper.rollbackAndClose(entityManager);
            LOGGER.log(Level.SEVERE, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private ID getIdentifier(T entity) throws IllegalAccessException {
        Field identifierField = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Identifier field is missing"));
        identifierField.setAccessible(true);
        return (ID) identifierField.get(entity);
    }

    private Class<T> getEntityClass() {
        final ParameterizedType type = (ParameterizedType) this.getClass().getGenericSuperclass();
        return (Class<T>) type.getActualTypeArguments()[0];
    }
}
