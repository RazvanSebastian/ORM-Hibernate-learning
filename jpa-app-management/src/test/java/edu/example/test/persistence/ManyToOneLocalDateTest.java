package edu.example.test.persistence;

import edu.example.test.entities.associations.manyToOne.Post;
import edu.example.test.entities.associations.manyToOne.PostComment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ManyToOneLocalDateTest extends AbstractTest {

    AtomicReference<Long> postComment1Id = new AtomicReference<>();

    @BeforeEach
    void init() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            entityManager.createQuery("DELETE FROM Post").executeUpdate();
            entityManager.createQuery("DELETE FROM PostComment").executeUpdate();

            Post post1 = new Post("Post1", LocalDate.of(2024, 1, 10));
            entityManager.persist(post1);

            PostComment postComment1 = new PostComment("Comment 11", LocalDate.of(2024, 1, 1), post1);
            PostComment postComment2 = new PostComment("Comment 12", LocalDate.of(2024, 2, 2), post1);
            PostComment postComment3 = new PostComment("Comment 13", LocalDate.of(2024, 3, 3), post1);

            entityManager.persist(postComment1);
            entityManager.persist(postComment2);
            entityManager.persist(postComment3);

            postComment1Id.set(postComment1.getId());

            Post post2 = new Post("Post2", LocalDate.of(2023, 1, 10));
            entityManager.persist(post2);

            PostComment postComment4 = new PostComment("Comment 21", LocalDate.of(2024, 2, 2), post2);
            entityManager.persist(postComment4);
        });
    }

    @Test
    void testRemove() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            var entity = entityManager.find(PostComment.class, postComment1Id.get());
            entity.getPost();
            entityManager.remove(entity);
        });
    }

    @Test
    void testBetween() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            LocalDate start = LocalDate.of(2024, 1, 1);
            LocalDate end = LocalDate.of(2024, 2, 20);

            List<PostComment> postCommentList = entityManager
                    .createQuery("SELECT pc FROM PostComment pc WHERE pc.publishedOn BETWEEN :start AND :end", PostComment.class)
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getResultList();

            assertEquals(3, postCommentList.size());
        });
    }

    @Test
    void testCompareThan() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            LocalDate min = LocalDate.of(2024, 3, 1);

            List<PostComment> postCommentList = entityManager
                    .createQuery("SELECT pc FROM PostComment pc WHERE pc.publishedOn > :min", PostComment.class)
                    .setParameter("min", min)
                    .getResultList();

            assertEquals(1, postCommentList.size());
        });
    }

    /**
     * Same for YEAR, DAYS, HOUR, MINUTE or SECOND
     */
    @Test
    void testCompareMonthThan() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            final int month = 2;

            List<PostComment> postCommentList = entityManager
                    .createQuery("SELECT pc FROM PostComment pc WHERE MONTH(pc.publishedOn) > :month", PostComment.class)
                    .setParameter("month", month)
                    .getResultList();

            assertEquals(1, postCommentList.size());
        });
    }

    @Test
    void testFindPostsWhitPostCommentInSpecificYear() {
        doInJPA(entityManagerFactorySupplierSupplier, entityManager -> {
            final int year = 2024;

            List<Post> postList = entityManager.createQuery("SELECT p FROM PostComment pc JOIN pc.post p WHERE YEAR(pc.publishedOn) = :year", Post.class)
                    .setParameter("year", year)
                    .getResultList();

            assertEquals(2, postList.size());
        });
    }
}


