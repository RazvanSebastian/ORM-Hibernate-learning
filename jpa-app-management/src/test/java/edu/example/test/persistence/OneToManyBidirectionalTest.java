package edu.example.test.persistence;

import edu.example.test.entities.associations.oneToMany.bidirectional.BidirectionalPerson;
import edu.example.test.entities.associations.oneToMany.bidirectional.BidirectionalPhone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

public class OneToManyBidirectionalTest extends AbstractTest {
    Long personId;

    @BeforeEach
    void init() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            entityManager.createQuery("DELETE FROM BidirectionalPerson").executeUpdate();
            entityManager.createQuery("DELETE FROM BidirectionalPhone").executeUpdate();

            BidirectionalPerson person = new BidirectionalPerson();
            entityManager.persist(person);

            BidirectionalPhone phone1 = new BidirectionalPhone("111");
            entityManager.persist(phone1);

            BidirectionalPhone phone2 = new BidirectionalPhone("222");
            entityManager.persist(phone2);

            person.addPhone(phone1);
            person.addPhone(phone2);

            personId = person.getId();

        });
    }

    @Test
    public void test() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            BidirectionalPerson person = entityManager.createQuery("SELECT p FROM BidirectionalPerson p JOIN FETCH p.phones WHERE p.id = :id", BidirectionalPerson.class)
                    .setParameter("id", personId)
                    .getSingleResult();

            List<BidirectionalPhone> phones = entityManager.createQuery("SELECT p FROM BidirectionalPhone p WHERE p.person.id = :id", BidirectionalPhone.class)
                    .setParameter("id", personId)
                    .getResultList();

            person.setPhones(phones);

        });
    }

}
