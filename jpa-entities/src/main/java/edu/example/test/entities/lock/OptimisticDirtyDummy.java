package edu.example.test.entities.lock;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@OptimisticLocking(type = OptimisticLockType.DIRTY)
@DynamicUpdate
@Entity
public class OptimisticDirtyDummy {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String name;

    private int countDown;


    public OptimisticDirtyDummy(String name, int countDown) {
        this.name = name;
        this.countDown = countDown;
    }
}
