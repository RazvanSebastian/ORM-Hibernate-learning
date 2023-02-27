package edu.example.test.persistence.associations.manyToMany;

import edu.example.test.entities.associations.manyToMany.Address;
import edu.example.test.entities.associations.manyToMany.Person;
import edu.example.test.persistence.BaseTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Best practices:
 * 1 Bidirectional best suited
 * 2 Owner side defines:
 *      - {@link javax.persistence.ManyToMany} contains for Cascade types, MERGE and PERSIST, only
 *      - {@link javax.persistence.JoinTable} annotation
 *      - Add and remove methods to synchronize both sides
 *      - Equals and hashcode methods (use whenever is possible NaturalId column or a group which might create something unique)
 *      - Use {@link java.util.Set} instead of {@link java.util.List}: best performance on deleting relation
 * 3. Inverse side defines:
 *      - {@link javax.persistence.ManyToMany} contains mappedBy
 *      - Equals and hashcode methods (use whenever is possible NaturalId column or a group which might create something unique)
 *      - Use {@link java.util.Set} instead of {@link java.util.List}: best performance on deleting relation
 */
public class ManyToManyTest extends BaseTest {

    @Test
    public void shouldPersistAndRemove() {
        // given
        Person person1 = new Person("12345");
        Person person2 = new Person("67890");

        Address address1 = new Address("Left street", "2", "654321");
        Address address2 = new Address("Right street", "1", "123456");

        // when save
        person1.addAddress(address1);
        person1.addAddress(address2);

        person2.addAddress(address1);

        entityManager.persist(person1);
        entityManager.persist(person2);
        entityManager.flush();

        // then save
        assertNotNull(person1.getId());
        assertNotNull(person2.getId());
        assertNotNull(address1.getId());
        assertNotNull(address2.getId());

        // when remove
        entityManager.clear();
        Person p = entityManager.find(Person.class, person1.getId());
        Address transientAddress = new Address("Left street", "2", "654321");
        p.removeAddress(transientAddress);
        entityManager.flush();
    }
}
