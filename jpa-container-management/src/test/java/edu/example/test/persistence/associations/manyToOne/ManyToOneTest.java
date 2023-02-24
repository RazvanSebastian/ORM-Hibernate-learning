package edu.example.test.persistence.associations.manyToOne;

import edu.example.test.entities.associations.manyToOne.Post;
import edu.example.test.entities.associations.manyToOne.PostComment;
import edu.example.test.persistence.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Most convenient way to map one-to-many relation.
 * {@link javax.persistence.ManyToOne} fetch is EAGER ny default. Make sure to set LAZY.
 */
public class ManyToOneTest extends BaseTest {

    private Long postId;
    private Long postCommentId;

    @BeforeEach
    public void clear() {
        entityManager.createQuery("DELETE FROM Post").executeUpdate();
        entityManager.createQuery("DELETE FROM PostComment").executeUpdate();

        Post post = new Post("Hibernate post");
        entityManager.persist(post);

        PostComment postComment = PostComment.builder()
                .comment("Like")
                .post(post)
                .build();
        entityManager.persist(postComment);

        entityManager.flush();

        postId = post.getId();
        postCommentId = postComment.getId();
    }

    @Test
    public void shouldAddPostAndPostComment() {
        Post post = new Post("Awesome post");
        PostComment postComment = PostComment.builder()
                .comment("Awesome post")
                .post(post)
                .build();

        entityManager.persist(post);
        entityManager.persist(postComment);

        entityManager.flush();

        assertTrue(entityManager.contains(post));
        assertTrue(entityManager.contains(postComment));
    }

    @Test
    public void shouldAddPostCommentToAnExistingPost() {
        Post post = entityManager.find(Post.class, postId);

        PostComment postComment = PostComment.builder()
                .comment("Awesome post")
                .post(post)
                .build();

        entityManager.persist(postComment);
        entityManager.flush();

        assertTrue(entityManager.contains(post));
        assertTrue(entityManager.contains(postComment));
    }

    @Test
    public void shouldRemoveAllPostCommentsForAPost() {
        entityManager.createQuery("DELETE FROM PostComment pc WHERE pc.post.id = :id")
                .setParameter("id", postId)
                .executeUpdate();
        entityManager.clear();

        assertNull(entityManager.find(PostComment.class, postCommentId));
    }

    @Test
    public void shouldRetrievePostOfPostComment() {
        entityManager.clear();

        Post post = (Post) entityManager.createQuery("SELECT pc.post FROM PostComment pc INNER JOIN pc.post WHERE pc.id = :id")
                .setParameter("id", postCommentId)
                .getSingleResult();

        assertNotNull(post);
    }
}
