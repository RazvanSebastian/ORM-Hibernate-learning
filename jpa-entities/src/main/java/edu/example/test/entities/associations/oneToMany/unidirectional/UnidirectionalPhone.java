package edu.example.test.entities.associations.oneToMany.unidirectional;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity(name = "UnidirectionalPhone")
public class UnidirectionalPhone {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String number;

    public UnidirectionalPhone() {
    }

    public UnidirectionalPhone(String number) {
        this.number = number;
    }

}
