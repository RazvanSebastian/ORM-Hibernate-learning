package edu.example.test.entities;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Setter
@Table
@Entity
public class Dummy implements JpaEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String value;

    private int countDown;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dummy dummy = (Dummy) o;
        return id.equals(dummy.id) && value.equals(dummy.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }
}
