package edu.example.test.persistence.associations.oneToMany;


import edu.example.test.entities.associations.oneToMany.bidirectional.BidirectionalPerson;
import edu.example.test.entities.associations.oneToMany.bidirectional.BidirectionalPhone;
import edu.example.test.persistence.BaseTest;
import org.junit.jupiter.api.Test;

/**
 * Rules:
 * 1. Owning side:
 *      - {@link javax.persistence.ManyToOne}
 *      - Set fetch to LAZY
 *      - Equals and hashcode methods (use whenever is possible NaturalId column or a group which might create something unique)
 * 2. Inverse side:
 *      - {@link javax.persistence.OneToMany}
 *      - Set fetch LAZY
 *      - Orphan removal: true
 *      - mappedBy
 *      - Add and remove methods to synchronize both sides
 *
 * Use cases:
 * - Really needs a collection of the association on the parent side.
 * - Few-to-Many
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
