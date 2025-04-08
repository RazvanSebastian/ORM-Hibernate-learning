package edu.example.test.persistence.associations.manyToMany;

import edu.example.test.entities.associations.manyToMany.Address;
import edu.example.test.entities.associations.manyToMany.Person;
import edu.example.test.persistence.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Best practices:
 * 1 Bidirectional best suited
 * 2 Owner side defines:
 *      - {@link jakarta.persistence.ManyToMany} contains for Cascade types, MERGE and PERSIST, only
 *      - {@link jakarta.persistence.JoinTable} annotation
 *      - Add and remove methods to synchronize both sides
 *      - Equals and hashcode methods (use whenever is possible NaturalId column or a group which might create something unique)
 *      - Use {@link java.util.Set} instead of {@link java.util.List}: best performance on deleting relation
 * 3. Inverse side defines:
 *      - {@link jakarta.persistence.ManyToMany} contains mappedBy
 *      - Equals and hashcode methods (use whenever is possible NaturalId column or a group which might create something unique)
 *      - Use {@link java.util.Set} instead of {@link java.util.List}: best performance on deleting relation
 */
class ManyToManyTest extends BaseTest {

    private Long personId;
    private Long address1Id;
    private Long address2Id;

    @BeforeEach
    void init() {
        entityManager.createQuery("DELETE FROM Person").executeUpdate();
        entityManager.createQuery("DELETE FROM Address").executeUpdate();

        Person person = new Person("12345");
        Address address1 = new Address("Left street", "2", "654321");
        Address address2 = new Address("Right street", "1", "123456");

        person.addAddress(address1);
        entityManager.persist(person);
        personId = person.getId();
        address1Id = address1.getId();

        entityManager.persist(address2);
        address2Id = address2.getId();

        entityManager.flush();
        entityManager.clear();
    }

    /**
     * BAD APPROACH: Hibernate will trigger 2 extra JOIN queries
     * 1. Retrieve the addresses (addAddress 1st line) -> person_address JOIN Address
     * 2. Retrieve the persons for the address (addAddress 2nd line) -> person_address JOIN Person
     */
    @Test
    void shouldAddToAssociation_incorrect() {
        Person person = entityManager.find(Person.class, personId);
        Address address2 = entityManager.find(Address.class, address2Id);
        person.addAddress(address2);

        entityManager.flush();
        entityManager.clear();
    }

    /**
     * BAD APPROACH: Hibernate will trigger 2 extra JOIN queries
     * 1. Retrieve the addresses (removeAddress 1st line) -> person_address JOIN Address
     * 2. Retrieve the persons for the address (removeAddress 2nd line) -> person_address JOIN Person
     */
    @Test
    void shouldRemoveFromAssociation_incorrect() {
        Person person = entityManager.find(Person.class, personId);
        Address address1 = entityManager.find(Address.class, address1Id);
        person.removeAddress(address1);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldAddToAssociation_correct() {
        Person person = entityManager.createQuery("SELECT p FROM Person p LEFT JOIN FETCH p.addresses WHERE p.id = :id", Person.class)
                .setParameter("id", personId)
                .getSingleResult();
        Address address = entityManager.createQuery("SELECT a FROM Address a LEFT JOIN FETCH a.persons WHERE a.id = :id", Address.class)
                .setParameter("id", address2Id)
                .getSingleResult();
        person.addAddress(address);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldRemoveFromAssociation_correct() {
        Person person = entityManager.createQuery("SELECT p FROM Person p LEFT JOIN FETCH p.addresses WHERE p.id = :id", Person.class)
                .setParameter("id", personId)
                .getSingleResult();
        Address address = entityManager.createQuery("SELECT a FROM Address a LEFT JOIN FETCH a.persons WHERE a.id = :id", Address.class)
                .setParameter("id", address1Id)
                .getSingleResult();
        person.removeAddress(address);

        entityManager.flush();
        entityManager.clear();
    }
}
