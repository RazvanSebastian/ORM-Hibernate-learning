package edu.example.test.persistence.dao.dummy;

import edu.example.test.dao.DummyJpaDao;
import edu.example.test.entities.Dummy;
import edu.example.test.persistence.dao.BaseJpaDao;

public class DummyJpaDaoImpl extends BaseJpaDao<Dummy, Long> implements DummyJpaDao {

    private static DummyJpaDao instance;

    private DummyJpaDaoImpl() {
    }

    public static DummyJpaDao getInstance() {
        if (instance == null) {
            instance = new DummyJpaDaoImpl();
        }
        return instance;
    }
}
