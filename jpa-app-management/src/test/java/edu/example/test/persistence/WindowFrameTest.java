package edu.example.test.persistence;

import edu.example.test.entities.associations.windowframe.Overtime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

class WindowFrameTest extends AbstractTest {

    @BeforeEach
    void beforeEach() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            entityManager.createQuery("DELETE FROM Overtime").executeUpdate();

            Overtime o1 = new Overtime("o1", 1);
            Overtime o2 = new Overtime("o2", 2);
            Overtime o3 = new Overtime("o3", 3);

            entityManager.persist(o1);
            entityManager.persist(o2);
            entityManager.persist(o3);
        });
    }

    @Test
    void test_retrieveAllOvertimeUntilSumOfQuantity() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            List<Object[]> result = entityManager.createNativeQuery(
                    "WITH numbered_rows AS (" +
                            "SELECT " +
                            "o.id as id,o.details as details,o.quantity as quantity," +
                            "ROW_NUMBER() OVER (ORDER BY o.id) as row_num," +
                            "SUM(o.quantity) OVER (ORDER BY o.id ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) as running_total " +
                            "FROM Overtime o" +
                            ")" +
                            "SELECT r.id, r.details, r.quantity " +
                            "FROM numbered_rows r " +
                            "WHERE r.running_total <= 3 OR r.running_total - r.quantity < 3",
                    Object[].class
            ).getResultList();
            result.stream().map(r -> new Overtime(
                    (Long) r[0],
                    (String) r[1],
                    (Integer) r[2])
            ).forEach(System.out::println);
        });
    }


    static class OvertimeWithSum {
        private Long id;
        private String details;
        private int quantity;
    }
}
