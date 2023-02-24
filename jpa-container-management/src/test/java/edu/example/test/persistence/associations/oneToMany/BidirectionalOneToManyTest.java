package edu.example.test.persistence.associations.oneToMany;


import edu.example.test.entities.associations.oneToMany.bidirectional.BidirectionalPerson;
import edu.example.test.entities.associations.oneToMany.bidirectional.BidirectionalPhone;
import edu.example.test.persistence.BaseTest;
import org.junit.jupiter.api.Test;

/**
 * Rules:
 * - Implements equals and hashCode on child side
 * - Use {@link org.hibernate.annotations.NaturalId} for euqlas and hashCode implementation if there are unique columns
 * - Keep in sync both sides, parent and child: create methods for add child and remove child
 *
 * Use cases:
 * - When we really need a collection of the association on the parent side.
 *
 */
public class BidirectionalOneToManyTest extends BaseTest {

    @Test
    public void shouldAddPersonAndPhones() {
        BidirectionalPerson person = new BidirectionalPerson();
        BidirectionalPhone phone1 = new BidirectionalPhone("123-456-789");
        BidirectionalPhone phone2 = new BidirectionalPhone("456-789-012");

        person.addPhone(phone1);
        person.addPhone(phone2);

        entityManager.persist(person);
        entityManager.flush();
    }

    @Test
    public void shouldRemovePhoneFromPerson() {
        BidirectionalPerson person = new BidirectionalPerson();
        BidirectionalPhone phone = new BidirectionalPhone("123-456-789");
        person.addPhone(phone);
        entityManager.persist(person);
        entityManager.flush();

        phone = person.getPhones().get(0);

        person.removePhone(phone);
        entityManager.flush();
    }
}
