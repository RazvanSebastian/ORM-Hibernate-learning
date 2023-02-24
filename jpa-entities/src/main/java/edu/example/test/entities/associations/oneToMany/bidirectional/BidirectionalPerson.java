package edu.example.test.entities.associations.oneToMany.bidirectional;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "BidirectionalPerson")
public class BidirectionalPerson {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            mappedBy = "person")
    private List<BidirectionalPhone> phones = new ArrayList<>();

    public void addPhone(BidirectionalPhone phone) {
        this.phones.add(phone);
        phone.setPerson(this);
    }

    public void removePhone(BidirectionalPhone phone) {
        phones.remove(phone);
        phone.setPerson(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<BidirectionalPhone> getPhones() {
        return phones;
    }

    public void setPhones(List<BidirectionalPhone> phones) {
        this.phones = phones;
    }
}
