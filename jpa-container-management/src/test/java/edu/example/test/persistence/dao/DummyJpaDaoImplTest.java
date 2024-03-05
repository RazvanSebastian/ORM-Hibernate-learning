package edu.example.test.persistence.dao;


import edu.example.test.dao.DummyJpaDao;
import edu.example.test.entities.Dummy;
import edu.example.test.persistence.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DummyJpaDaoImplTest extends BaseTest {

    @Autowired
    private DummyJpaDao dummyJpaDao;

    @BeforeEach
    public void clear() {
        dummyJpaDao.clear();
    }

    @Test
    public void testUniqueConstrain() {
        // given
        Dummy dummy = new Dummy();
        dummy.setValue("value");

        dummyJpaDao.save(dummy);

        Dummy dummy2 = new Dummy();
        dummy2.setValue("value");
        dummyJpaDao.save(dummy2);
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
    public void shouldSaveAll() {
        // given
        List<Dummy> dummies = IntStream.rangeClosed(1, 10)
                .mapToObj(i -> new Dummy())
                .collect(Collectors.toList());

        // when
        dummyJpaDao.saveAll(dummies);

        // then
        assertEquals(10, dummyJpaDao.count());
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
    public void shouldClear() {
        // given


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
    public void shouldDeleteDetached() {
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
        Assertions.assertNull(dummyJpaDao.find(dummyToDelete.getId()));
    }


}
