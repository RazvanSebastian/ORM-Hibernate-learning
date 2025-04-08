package edu.example.test.entities.associations.manyToOne;

import lombok.*;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name = "PostComment")
public class PostComment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    private String comment;

    @Temporal(TemporalType.DATE)
    private LocalDate publishedOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "post_id", foreignKey = @ForeignKey(name = "POST_ID_FK"))
    private Post post;

    public PostComment(String comment) {
        this.comment = comment;
    }

    public PostComment(String comment, LocalDate publishedOn, Post post) {
        this.comment = comment;
        this.publishedOn = publishedOn;
        this.post = post;
    }
}
