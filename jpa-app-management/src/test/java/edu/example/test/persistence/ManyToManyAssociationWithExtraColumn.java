package edu.example.test.persistence;

import edu.example.test.entities.associations.manyToManyWithExtraColumns.Employee;
import edu.example.test.entities.associations.manyToManyWithExtraColumns.EmployeePositionHistory;
import edu.example.test.entities.associations.manyToManyWithExtraColumns.Position;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

class ManyToManyAssociationWithExtraColumn extends AbstractTest {

    @Test
    void testInsertAndDelete() {
        AtomicReference<Long> positionToRemove = new AtomicReference<>();
        AtomicReference<Long> positionToUpdate = new AtomicReference<>();
        AtomicReference<Long> employeeId = new AtomicReference<>();

        // insert
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            Employee employee = new Employee("New Employee");

            Position javaDev = new Position("Java developer");
            Position angularDev = new Position("Angular developer");
            Position dbDev = new Position("Database developer");

            entityManager.persist(javaDev);
            entityManager.persist(angularDev);
            entityManager.persist(dbDev);

            employee.addPosition(javaDev);
            employee.addPosition(angularDev);
            employee.addPosition(dbDev);

            entityManager.persist(employee);

            positionToRemove.set(dbDev.getId());
            positionToUpdate.set(javaDev.getId());
            employeeId.set(employee.getId());
        });

        // remove
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            Position position = entityManager.find(Position.class, positionToRemove.get());
            Employee employee = getEmployee(entityManager, employeeId.get());
            employee.removePosition(position);
        });

        // update
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            Employee employee = getEmployee(entityManager, employeeId.get());
            for (EmployeePositionHistory positionHistory : employee.getPositionHistories()) {
                if (positionHistory.getPosition().getId().equals(positionToUpdate.get())) {
                    positionHistory.setEndDate(new Date());
                    break;
                }
            }
        });
    }

   private Employee getEmployee(EntityManager entityManager, Long id) {
        return entityManager.createQuery("select e from Employee e " +
                        "join fetch e.positionHistories ph " +
                        "join fetch ph.position " +
                        "where e.id = :employeeId", Employee.class)
                .setParameter("employeeId", id)
                .getSingleResult();
    }
}
