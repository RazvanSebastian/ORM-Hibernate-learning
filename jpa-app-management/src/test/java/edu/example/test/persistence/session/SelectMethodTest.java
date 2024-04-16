package edu.example.test.persistence.session;

import edu.example.test.entities.associations.manyToOne.Post;
import edu.example.test.entities.associations.manyToOne.PostComment;
import edu.example.test.persistence.AbstractTest;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;

class SelectMethodTest extends AbstractTest {
    AtomicReference<Long> postId = new AtomicReference<>();
    AtomicReference<Long> comment1Id = new AtomicReference<>();
    AtomicReference<Long> comment2Id = new AtomicReference<>();

    Statistics statistics;

    @BeforeEach
    void initialize() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            entityManager.createQuery("DELETE FROM Post");
            entityManager.createQuery("DELETE FROM PostComment");

            Post post = new Post("New post");

            entityManager.persist(post);

            PostComment postComment1 = new PostComment("Comm 1");
            postComment1.setPost(post);

            PostComment postComment2 = new PostComment("Comm 2");
            postComment2.setPost(post);

            entityManager.persist(postComment1);
            entityManager.persist(postComment2);

            postId.set(post.getId());
            comment1Id.set(postComment1.getId());
            comment2Id.set(postComment2.getId());

            statistics = entityManager.unwrap(Session.class).getSessionFactory().getStatistics();
            statistics.clear(); // clear all other queries
        });
    }

    @Test
    void testSelectByForeignKey() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            var query = "SELECT entity FROM PostComment entity WHERE entity.post.id = :postId";
            List<PostComment> postComments = entityManager.createQuery(query, PostComment.class)
                    .setParameter("postId", postId.get())
                    .getResultList();

            String[] queries = statistics.getQueries();

            assertThat(queries).hasSize(1);
            assertThat(queries[0]).isEqualTo(query);
            assertThat(postComments).hasSize(2);
        });

    }
}
