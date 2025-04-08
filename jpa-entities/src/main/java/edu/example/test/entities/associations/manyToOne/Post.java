package edu.example.test.entities.associations.manyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity(name = "Post")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String title;

    private String description;

    private LocalDate createdOn;

    public Post(String title) {
        this.title = title;
    }

    public Post(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Post(String title, LocalDate createdOn) {
        this.title = title;
        this.createdOn = createdOn;
    }
}
