package edu.example.test.persistence;

import edu.example.test.persistence.config.RootConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Transactional
@ContextConfiguration(classes = RootConfiguration.class)
@ExtendWith(SpringExtension.class)
public class BaseTest {

    @PersistenceContext
    protected EntityManager entityManager;
}
