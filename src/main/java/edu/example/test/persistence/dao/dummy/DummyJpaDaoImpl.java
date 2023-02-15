package edu.example.test.persistence.dao.dummy;

import edu.example.test.persistence.dao.BaseJpaDao;
import edu.example.test.persistence.entity.Dummy;

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
