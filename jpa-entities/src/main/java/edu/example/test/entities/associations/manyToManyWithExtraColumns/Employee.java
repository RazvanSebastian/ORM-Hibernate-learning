package edu.example.test.entities.associations.manyToManyWithExtraColumns;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@Entity
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "employee")
    private List<EmployeePositionHistory> positionHistories = new ArrayList<>();

    public Employee() {
    }

    public Employee(String name) {
        this.name = name;
    }

    public void addPosition(Position position) {
        EmployeePositionHistory history = new EmployeePositionHistory(
                new Date(),
                null,
                this,
                position
        );
        this.positionHistories.add(history);
    }

    public void removePosition(Position position) {
        var iterator = this.positionHistories.iterator();
        while (iterator.hasNext()) {
            EmployeePositionHistory history = iterator.next();
            if (this.equals(history.getEmployee()) && history.getPosition().equals(position)) {
                iterator.remove();
                history.setPosition(null);
                history.setEmployee(null);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id) && Objects.equals(name, employee.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
