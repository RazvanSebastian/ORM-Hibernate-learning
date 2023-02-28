package edu.example.test.persistence.session;

import edu.example.test.entities.Dummy;
import edu.example.test.persistence.AbstractTest;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;
import static org.junit.jupiter.api.Assertions.*;

public class DifferentSessionsEqualityTest extends AbstractTest {

    @Test
    public void givenStoredObjectIntoDB_whenGetIsCalledFromSameSession_thenReturnSameInstanceForEntity() {
        Dummy dummy = new Dummy();
        doInHibernate(sessionFactorySupplier, session -> {
            // given
            dummy.setValue("test");
            session.persist(dummy);
        });
        doInHibernate(sessionFactorySupplier, session -> {
            // when
            Dummy entity1 = session.get(Dummy.class, dummy.getId());
            Dummy entity2 = session.get(Dummy.class, dummy.getId());

            // then
            assertTrue(entity1 == entity2);
        });
    }

    @Test
    public void givenStoredObjectIntoDB_whenGetIsCalledOnDifferentSessions_thenReturnDifferentInstancesButObjectsShouldBeEquals() {
        // given
        Dummy dummy = new Dummy();
        doInHibernate(sessionFactorySupplier, session -> {
            dummy.setValue("test");
            session.persist(dummy);
        });

        // when
        AtomicReference<Dummy> dummyS1 = new AtomicReference<>();
        doInHibernate(sessionFactorySupplier, session -> {
            dummyS1.set(session.find(Dummy.class, dummy.getId()));
        });

        AtomicReference<Dummy> dummyS2 = new AtomicReference<>();
        doInHibernate(sessionFactorySupplier, session -> {
            dummyS2.set(session.find(Dummy.class, dummy.getId()));
        });

        // then
        assertFalse(dummyS1.get() == dummyS2.get());
        assertEquals(dummyS1.get(), dummyS2.get());
    }
}
