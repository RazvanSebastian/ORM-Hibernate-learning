package edu.example.test.persistence;

import edu.example.test.entities.associations.manyToMany.Address;
import edu.example.test.entities.associations.manyToMany.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ManyToManyJoinTest extends AbstractTest {

    AtomicLong personId = new AtomicLong();

    @BeforeEach
    void beforeEach() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            entityManager.createQuery("DELETE FROM Person").executeUpdate();
            entityManager.createQuery("DELETE FROM Address").executeUpdate();

            Person person = new Person("John");

            Address a1 = new Address("street1", "city1", "country1");
            Address a2 = new Address("street2", "city2", "country2");
            Address a3 = new Address("street3", "city3", "country3");

            person.addAddress(a1);
            person.addAddress(a2);

            entityManager.persist(person);
            entityManager.persist(a3);

            entityManager.flush();

            personId.set(person.getId());
        });
    }

    @Test
    public void test() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            List<Address> addresses = entityManager.createQuery("SELECT a FROM Address a " +
                            "RIGHT JOIN a.persons p " +
                            "WHERE p.id = :id", Address.class)
                    .setParameter("id", personId.get())
                    .getResultList();

            assertEquals(2, addresses.size());
            assertEquals(1, addresses.get(0).getPersons().size());
            assertEquals((long) addresses
                    .get(1)
                    .getPersons()
                    .stream()
                    .filter(p -> p.getId().equals(personId.get()))
                    .findFirst()
                    .get()
                    .getId(), personId.get());
        });
    }

}
