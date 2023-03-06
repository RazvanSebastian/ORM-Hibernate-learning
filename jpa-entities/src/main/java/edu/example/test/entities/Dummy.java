package edu.example.test.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Table
@Entity
public class Dummy implements JpaEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue
    private Long id;

    private String value;

    private int countDown;

    public Dummy(int countDown) {
        this.countDown = countDown;
    }
}
