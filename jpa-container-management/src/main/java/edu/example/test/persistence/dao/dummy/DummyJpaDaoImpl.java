package edu.example.test.persistence.dao.dummy;

import edu.example.test.dao.DummyJpaDao;
import edu.example.test.entities.Dummy;
import edu.example.test.persistence.dao.BaseJpaDao;
import org.springframework.stereotype.Repository;

@Repository
public class DummyJpaDaoImpl extends BaseJpaDao<Dummy, Long> implements DummyJpaDao {
}
