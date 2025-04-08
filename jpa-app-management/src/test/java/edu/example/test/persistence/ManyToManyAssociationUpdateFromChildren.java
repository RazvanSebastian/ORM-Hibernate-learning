package edu.example.test.persistence;

import edu.example.test.entities.associations.manyToMany.Address;
import edu.example.test.entities.associations.manyToMany.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

class ManyToManyAssociationUpdateFromChildren extends AbstractTest {

    Long addressId;
    Set<Long> updatedListOfPersonsId;
    Long addedPersonId;
    Long removedPersonId;

    @BeforeEach
    void beforeEach() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            entityManager.createQuery("DELETE FROM Person").executeUpdate();
            entityManager.createQuery("DELETE FROM Address").executeUpdate();

            Person p1 = new Person("P1");
            Person p2 = new Person("P2");
            Person p3 = new Person("P3");

            Address address = new Address("Street", "City", "Country");

            p1.addAddress(address);
            p2.addAddress(address);

            entityManager.persist(p1);
            entityManager.persist(p2);
            entityManager.persist(p3);

            addressId = address.getId();

            // Removed p1 and added p3
            addedPersonId = p3.getId();
            removedPersonId = p1.getId();
            updatedListOfPersonsId = Set.of(p2.getId(), p3.getId());
        });
    }

    @Test
    void updateAssociationFromChildren() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            entityManager.createNativeQuery("DELETE FROM person_address WHERE person_id = :id")
                    .setParameter("id", removedPersonId)
                            .executeUpdate();
            entityManager.createNativeQuery("INSERT INTO person_address (address_id, person_id) VALUES (:addressId, :personId)")
                    .setParameter("addressId", addressId)
                    .setParameter("personId", addedPersonId)
                    .executeUpdate();

            Address a = entityManager.createQuery("SELECT a FROM Address a LEFT JOIN FETCH a.persons WHERE a.id = :id", Address.class)
                    .setParameter("id", addressId)
                    .getSingleResult();

            assertTrue(a.getPersons().size() == 2);
            assertTrue(a.getPersons().stream().anyMatch(p -> p.getId().equals(addedPersonId)));
            assertFalse(a.getPersons().stream().anyMatch(p -> p.getId().equals(removedPersonId)));
        });
    }


}
