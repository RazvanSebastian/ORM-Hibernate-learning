package edu.example.test.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Table
@Entity
public class Dummy implements JpaEntity {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

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
