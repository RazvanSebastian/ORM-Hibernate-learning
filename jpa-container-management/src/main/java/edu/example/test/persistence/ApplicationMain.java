package edu.example.test.persistence;

import edu.example.test.dao.DummyJpaDao;
import edu.example.test.persistence.config.RootConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationMain {

    public static void main(String... args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(RootConfiguration.class);
        DummyJpaDao bean = ctx.getBean(DummyJpaDao.class);
        System.out.println(bean);
    }
}
