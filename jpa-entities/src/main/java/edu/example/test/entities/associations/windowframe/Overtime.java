package edu.example.test.entities.associations.windowframe;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "overtime")
@Entity(name = "Overtime")
public class Overtime {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String details;

    private int quantity;

    public Overtime(String details, int quantity) {
        this.details = details;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Overtime{" +
                "id=" + id +
                ", details='" + details + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
