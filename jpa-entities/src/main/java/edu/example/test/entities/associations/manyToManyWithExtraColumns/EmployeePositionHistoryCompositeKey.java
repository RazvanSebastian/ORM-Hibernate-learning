package edu.example.test.entities.associations.manyToManyWithExtraColumns;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class EmployeePositionHistoryCompositeKey implements Serializable {

    private static final long serialVersionUID = 5124043035204862054L;
    private Long employeeId;
    private Long positionId;

    private EmployeePositionHistoryCompositeKey(){}

    public EmployeePositionHistoryCompositeKey(Long employeeId, Long positionId) {
        this.employeeId = employeeId;
        this.positionId = positionId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeePositionHistoryCompositeKey that = (EmployeePositionHistoryCompositeKey) o;
        return Objects.equals(employeeId, that.employeeId) && Objects.equals(positionId, that.positionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, positionId);
    }
}
