package edu.example.test.persistence;

import edu.example.test.entities.associations.manyToManyWithExtraColumns.Employee;
import edu.example.test.entities.associations.manyToManyWithExtraColumns.EmployeePositionHistory;
import edu.example.test.entities.associations.manyToManyWithExtraColumns.Position;
import org.junit.jupiter.api.Test;

import jakarta.persistence.EntityManager;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ManyToManyAssociationWithExtraColumnTest extends AbstractTest {

    @Test
    void testInsertSearchAndDelete() {
        AtomicReference<Long> positionToSearchForEmployees = new AtomicReference<>();
        AtomicReference<Long> positionToRemove = new AtomicReference<>();
        AtomicReference<Long> positionToUpdate = new AtomicReference<>();
        AtomicReference<Long> employeeId = new AtomicReference<>();

        // insert
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            Employee employee = new Employee("New Employee1");
            Employee employee1 = new Employee("New Employee2");

            Position javaDev = new Position("Java developer");
            Position angularDev = new Position("Angular developer");
            Position dbDev = new Position("Database developer");

            entityManager.persist(javaDev);
            entityManager.persist(angularDev);
            entityManager.persist(dbDev);

            entityManager.flush();
            entityManager.clear();

            employee.addPosition(entityManager.getReference(Position.class, javaDev.getId()));
            employee.addPosition(entityManager.getReference(Position.class, angularDev.getId()));
            employee.addPosition(entityManager.getReference(Position.class, dbDev.getId()));

            employee1.addPosition(entityManager.getReference(Position.class, javaDev.getId()));

            entityManager.persist(employee);
            entityManager.persist(employee1);

            positionToSearchForEmployees.set(javaDev.getId());
            positionToRemove.set(dbDev.getId());
            positionToUpdate.set(javaDev.getId());
            employeeId.set(employee.getId());
        });

        // get all employees with a specific position
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            List<Employee> employees = getEmployees(entityManager, positionToSearchForEmployees.get());
            assertEquals(2, employees.size());
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

    private Employee getEmployee(EntityManager entityManager, Long employeeId) {
        return entityManager.createQuery("select e from Employee e " +
                        "join fetch e.positionHistories ph " +
                        "join fetch ph.position " +
                        "where e.id = :employeeId", Employee.class)
                .setParameter("employeeId", employeeId)
                .getSingleResult();
    }

    private List<Employee> getEmployees(EntityManager entityManager, Long positionId) {
        return entityManager.createQuery("select e from Employee e " +
                        "join fetch e.positionHistories ph " +
                        "where ph.position.id = :positionId", Employee.class)
                .setParameter("positionId", positionId)
                .getResultList();
    }
}
