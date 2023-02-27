package edu.example.test.entities.associations.oneToMany.bidirectional;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity(name = "BidirectionalPerson")
public class BidirectionalPerson {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany(
            fetch = FetchType.LAZY,
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
}
