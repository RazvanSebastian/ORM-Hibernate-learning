package edu.example.test.persistence.associations.oneToMany;


import edu.example.test.entities.associations.oneToMany.unidirectional.UnidirectionalPerson;
import edu.example.test.entities.associations.oneToMany.unidirectional.UnidirectionalPhone;
import edu.example.test.persistence.BaseTest;
import org.junit.jupiter.api.Test;

/**
 * Rules:
 * - Use {@link javax.persistence.JoinColumn } in order to tells Hibernate to don't treat as ManyToMany and skip creation of intermediary table
 * <p>
 * Behaviours:
 * - On child remove: firstly Hibernate will trigger an UPDATE to set the FK with null and then triggers DELETE
 * - On child persist: firstly Hibernate will trigger a SAVE of children row and then triggers UPDATE to set FK with the id of the parent
 * <p>
 * Use cases:
 * - NEVER
 * <p>
 * Best practice:
 * - Use bidirectional OneToMany OR ManyToOne
 */
public class UnidirectionalOneToManyTest extends BaseTest {

    @Test
    public void addPersonAndPhones() {
        UnidirectionalPerson person = new UnidirectionalPerson();
        person.getPhones().add(new UnidirectionalPhone("123-456-789"));
        person.getPhones().add(new UnidirectionalPhone("456-789-012"));

        entityManager.persist(person);
        entityManager.flush();
    }

    @Test
    public void removePhoneFromAPersonByIds() {
        UnidirectionalPerson person = new UnidirectionalPerson();
        UnidirectionalPhone phone = new UnidirectionalPhone("123-456-789");
        person.getPhones().add(phone);
        entityManager.persist(person);
        entityManager.flush();

        person.getPhones().remove(0);
        entityManager.flush();
    }
}
