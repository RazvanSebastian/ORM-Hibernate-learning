package edu.example.test.entities.associations.oneToMany.unidirectional;

import javax.persistence.*;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
