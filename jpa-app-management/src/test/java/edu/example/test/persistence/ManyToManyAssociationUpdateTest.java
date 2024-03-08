package edu.example.test.persistence;

import edu.example.test.entities.associations.manyToMany.Address;
import edu.example.test.entities.associations.manyToMany.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

public class ManyToManyAssociationUpdateTest extends AbstractTest {
    AtomicLong personId = new AtomicLong();

    AtomicReference<List<Long>> updatedList = new AtomicReference<>();

    @BeforeEach
    void beforeEach() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            entityManager.createQuery("DELETE FROM Person").executeUpdate();
            entityManager.createQuery("DELETE FROM Address").executeUpdate();

            Person person = new Person();

            IntStream.range(1, 100)
                    .mapToObj(i -> new Address("Street" + i, String.valueOf(i), String.valueOf(i)))
                    .collect(Collectors.toSet())
                    .forEach(address -> person.getAddresses().add(address));

            entityManager.persist(person);

            Address newAddress = new Address();
            entityManager.persist(newAddress);

            entityManager.flush();

            updatedList.set(Stream.concat(person.getAddresses().stream().map(Address::getId).skip(1), Stream.of(newAddress.getId())).collect(Collectors.toList()));
            personId.set(person.getId());
        });
    }

    @Test
    void testUpdateAssociation() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            Person currentPerson = retrieveEntityToUpdate(
                    entityManager,
                    "id",
                    personId.get(),
                    List.of("addresses"),
                    Person.class);

            Set<Address> currentAddressAssociation = currentPerson.getAddresses();
            Set<Address> newAssociations = retrieveProvidedUpdatedAssociation(
                    entityManager,
                    "id",
                    updatedList.get(),
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

    private static <T> Set<T> retrieveProvidedUpdatedAssociation(EntityManager entityManager, String rootIdName, List<Long> ids, Class<T> clazz) {
        CriteriaQuery<T> criteriaQuery = entityManager.getCriteriaBuilder().createQuery(clazz);
        Root<T> root = criteriaQuery.from(clazz);

        criteriaQuery.select(root).where(root.get(rootIdName).in(ids));

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
