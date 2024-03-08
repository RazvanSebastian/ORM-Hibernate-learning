package edu.example.test.entities.associations.manyToManyWithExtraColumns;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
public class EmployeePositionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private Date startDate;
    private Date endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    private Position position;

    public EmployeePositionHistory() {
    }

    public EmployeePositionHistory(Date startDate, Date endDate, Employee employee, Position position) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.employee = employee;
        this.position = position;
    }
}
