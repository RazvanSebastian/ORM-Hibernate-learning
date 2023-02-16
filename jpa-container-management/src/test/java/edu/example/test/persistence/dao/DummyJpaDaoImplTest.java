package edu.example.test.persistence.dao;


import edu.example.test.dao.DummyJpaDao;
import edu.example.test.entities.Dummy;
import edu.example.test.persistence.config.RootConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.stream.IntStream;

@ContextConfiguration(classes = RootConfiguration.class)
@ExtendWith(SpringExtension.class)
public class DummyJpaDaoImplTest {

    @Autowired
    private DummyJpaDao dummyJpaDao;

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    public void clear() {
        dummyJpaDao.clear();
    }

    @Test
    public void shouldSave() {
        // given
        Dummy dummy = new Dummy();
        dummy.setValue("should save");

        // when
        dummyJpaDao.save(dummy);

        // then
        Assertions.assertNotNull(dummy.getId());
        Assertions.assertNotNull(entityManager.contains(dummy));
    }

    @Test
    public void shouldFindById() {
        // given
        Dummy expected = new Dummy();
        expected.setValue("should save");
        dummyJpaDao.save(expected);

        // when
        Dummy actual = dummyJpaDao.find(expected.getId());

        // then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(actual, expected);
    }

    @Test
    public void shouldFindAll() {
        // given
        IntStream.rangeClosed(1, 10).forEach(value -> dummyJpaDao.save(new Dummy()));

        // when
        Collection<Dummy> actualList = dummyJpaDao.findAll();

        // then
        Assertions.assertNotNull(actualList);
        Assertions.assertEquals(actualList.size(), 10);
    }

    @Test
    public void shouldDelete() {
        // given
        Dummy dummyToDelete = new Dummy();
        dummyToDelete.setValue("should delete");
        dummyJpaDao.save(dummyToDelete);

        // when
        dummyJpaDao.delete(dummyToDelete);

        // then
        Assertions.assertFalse(entityManager.contains(dummyToDelete));
        Assertions.assertNull(dummyJpaDao.find(dummyToDelete.getId()));
    }

    @Test
    public void shouldDeleteDetached(){
        // given
        Dummy dummyToDelete = new Dummy();
        dummyToDelete.setValue("should delete");
        dummyJpaDao.save(dummyToDelete);
        entityManager.detach(dummyToDelete);

        Assertions.assertFalse(entityManager.contains(dummyToDelete));

        // when
        dummyJpaDao.delete(dummyToDelete);

        // then
        Assertions.assertFalse(entityManager.contains(dummyToDelete));
        Assertions.assertNull(dummyJpaDao.find(dummyToDelete.getId()));    }
}
