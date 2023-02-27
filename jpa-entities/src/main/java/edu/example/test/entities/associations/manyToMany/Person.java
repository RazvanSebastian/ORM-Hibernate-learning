package edu.example.test.entities.associations.manyToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @NaturalId
    private String codeNumber;

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "person_address",
            joinColumns = {@JoinColumn(name = "person_id")},
            inverseJoinColumns = {@JoinColumn(name = "address_id")}
    )
    private Set<Address> addresses = new HashSet<>();

    public Person(String codeNumber) {
        this.codeNumber = codeNumber;
    }

    public void addAddress(Address address) {
        addresses.add(address);
        address.getPersons().add(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.getPersons().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return codeNumber.equals(person.codeNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeNumber);
    }
}
