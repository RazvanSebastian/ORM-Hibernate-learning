package edu.example.test.entities.associations.oneToMany.bidirectional;

import org.hibernate.Hibernate;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "BidirectionalPhone")
public class BidirectionalPhone {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @NaturalId
    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    private BidirectionalPerson person;

    public BidirectionalPhone() {
    }

    public BidirectionalPhone(String number) {
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

    public BidirectionalPerson getPerson() {
        return person;
    }

    public void setPerson(BidirectionalPerson person) {
        this.person = person;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        BidirectionalPhone phone = (BidirectionalPhone) o;
        return number != null && Objects.equals(number, phone.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }
}
