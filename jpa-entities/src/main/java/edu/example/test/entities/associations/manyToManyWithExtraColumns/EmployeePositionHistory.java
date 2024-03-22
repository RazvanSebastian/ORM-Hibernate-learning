package edu.example.test.entities.associations.manyToManyWithExtraColumns;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
public class EmployeePositionHistory {

    @EmbeddedId
    private EmployeePositionHistoryCompositeKey id;

    private Date startDate;
    private Date endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("employeeId")
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("positionId")
    private Position position;

    public EmployeePositionHistory() {
    }

    public EmployeePositionHistory(Date startDate, Date endDate, Employee employee, Position position) {
        this.id = new EmployeePositionHistoryCompositeKey(employee.getId(), position.getId());
        this.startDate = startDate;
        this.endDate = endDate;
        this.employee = employee;
        this.position = position;
    }
}
