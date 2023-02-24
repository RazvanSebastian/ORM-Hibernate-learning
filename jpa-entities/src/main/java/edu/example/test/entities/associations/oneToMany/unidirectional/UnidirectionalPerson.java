package edu.example.test.entities.associations.oneToMany.unidirectional;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "UnidirectionalPerson")
public class UnidirectionalPerson {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "person_id")
    private List<UnidirectionalPhone> phones = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<UnidirectionalPhone> getPhones() {
        return phones;
    }

    public void setPhones(List<UnidirectionalPhone> phones) {
        this.phones = phones;
    }
}
