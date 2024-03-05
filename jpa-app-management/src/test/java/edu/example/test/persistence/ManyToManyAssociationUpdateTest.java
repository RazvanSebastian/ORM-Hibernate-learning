package edu.example.test.persistence;

import edu.example.test.entities.associations.manyToMany.Address;
import edu.example.test.entities.associations.manyToMany.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

public class ManyToManyAssociationUpdateTest extends AbstractTest {
    AtomicLong personId = new AtomicLong();
    AtomicLong address1Id = new AtomicLong();
    AtomicLong address2Id = new AtomicLong();
    AtomicLong address3Id = new AtomicLong();

    @BeforeEach
    void beforeEach() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            entityManager.createQuery("DELETE FROM Person").executeUpdate();
            entityManager.createQuery("DELETE FROM Address").executeUpdate();

            Person person = new Person();

            Address address1 = new Address("Street1", "1", "1");
            Address address2 = new Address("Street2", "2", "2");
            Address address3 = new Address("Street3", "3", "3");

            person.getAddresses().add(address1);
            person.getAddresses().add(address2);

            entityManager.persist(person);
            entityManager.persist(address3);

            entityManager.flush();
            personId.set(person.getId());
            address1Id.set(address1.getId());
            address2Id.set(address2.getId());
            address3Id.set(address3.getId());
        });
    }

    @Test
    void testUpdateAssociation() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            // given
            List<Long> givenUpdatedAssociationIdsList = Arrays.asList(address2Id.get(), address3Id.get());
            Person currentPerson = retrieveEntityToUpdate(
                    entityManager,
                    "id",
                    personId.get(),
                    List.of("addresses"),
                    Person.class);

            Set<Address> currentAddressAssociation = currentPerson.getAddresses();
            Set<Address> newAssociations = retrieveProvidedUpdatedAssociation(
                    entityManager,
                    givenUpdatedAssociationIdsList,
                    Address.class
            );

            updateAssociation(currentPerson.getAddresses(), newAssociations, currentAddressAssociation::remove, currentAddressAssociation::add);
        });
    }

    private static <T> T retrieveEntityToUpdate(EntityManager entityManager, String rootIdName, Long rootId, List<String> rootAssociationNames, Class<T> clazz) {
        CriteriaQuery<T> criteriaQuery = entityManager.getCriteriaBuilder().createQuery(clazz);

        Root<T> root = criteriaQuery.from(clazz);

        rootAssociationNames.forEach(root::fetch);

        criteriaQuery.select(root).where(entityManager.getCriteriaBuilder().equal(root.get(rootIdName), rootId));

        return entityManager.createQuery(criteriaQuery).getSingleResult();
    }

    private static <T> Set<T> retrieveProvidedUpdatedAssociation(EntityManager entityManager, List<Long> ids, Class<T> clazz) {
        CriteriaQuery<T> criteriaQuery = entityManager.getCriteriaBuilder().createQuery(clazz);
        Root<T> root = criteriaQuery.from(clazz);

        criteriaQuery.select(root).where(root.get("id").in(ids));

        return entityManager.createQuery(criteriaQuery).getResultStream().collect(Collectors.toSet());
    }

    public static <T> void updateAssociation(Set<T> currentAssociations, Set<T> newAssociations, Consumer<T> removeFromCurrent, Consumer<T> addToCurrent) {
        Set<T> associationsToRemove = currentAssociations
                .stream()
                .filter(userEntity -> !newAssociations.contains(userEntity))
                .collect(Collectors.toSet());

        associationsToRemove.forEach(removeFromCurrent);

        newAssociations
                .stream()
                .filter(newUserToAdd -> !currentAssociations.contains(newUserToAdd))
                .forEach(addToCurrent);
    }
}
