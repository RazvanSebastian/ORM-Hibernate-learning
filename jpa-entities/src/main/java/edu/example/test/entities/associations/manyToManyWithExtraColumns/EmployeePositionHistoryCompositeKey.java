package edu.example.test.entities.associations.manyToManyWithExtraColumns;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class EmployeePositionHistoryCompositeKey implements Serializable {

    private static final long serialVersionUID = 5124043035204862054L;
    private Long employeeId;
    private Long positionId;
}
