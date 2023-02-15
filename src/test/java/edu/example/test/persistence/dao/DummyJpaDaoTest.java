package edu.example.test.persistence.dao;

import edu.example.test.persistence.dao.dummy.DummyJpaDao;
import edu.example.test.persistence.dao.dummy.DummyJpaDaoImpl;
import edu.example.test.persistence.entity.Dummy;
import org.junit.jupiter.api.*;

import java.util.stream.IntStream;

public class DummyJpaDaoTest {

    static DummyJpaDao dummyJpaDao;

    @BeforeAll
    static void init() {
        dummyJpaDao = DummyJpaDaoImpl.getInstance();
    }

    @AfterEach
    void clear() {
        dummyJpaDao.clear();
    }

    @Test
    public void shouldSave() {
        // given
        Dummy dummy = new Dummy();
        dummy.setValue("valueSave");

        // when
        dummyJpaDao.save(dummy);

        // then
        Assertions.assertNotNull(dummy.getId());
    }

    @Test
    public void shouldFindById() {
        // given
        Dummy expected = new Dummy();
        expected.setValue("valueFindById");
        dummyJpaDao.save(expected);

        // when
        Dummy actual = dummyJpaDao.find(expected.getId());

        // then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void shouldFindAll() {
        // given
        IntStream.rangeClosed(1, 10).forEach(value -> dummyJpaDao.save(new Dummy()));

        // when + then
        Assertions.assertEquals(dummyJpaDao.findAll().size(), 10);
    }

    @Test
    public void shouldDelete() {
        // given
        Dummy dummyToDelete = new Dummy();
        dummyToDelete.setValue("valueDelete");
        dummyJpaDao.save(dummyToDelete);

        // when
        dummyJpaDao.delete(dummyToDelete);

        // then
        Assertions.assertNotNull(dummyToDelete);
        Assertions.assertNull(dummyJpaDao.find(dummyToDelete.getId()));
    }

}
