package edu.example.test.persistence.associations.oneToOne;

import edu.example.test.entities.associations.oneToOne.User;
import edu.example.test.entities.associations.oneToOne.UserDetails;
import edu.example.test.persistence.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * - Best way to implement is by using unidirectional association.
 * - Use @{@link javax.persistence.MapsId} to have PARENT_PK == CHILD_PK == CHILD_FK
 * - Set {@link javax.persistence.OneToOne} with fetch LAZY
 * <p>
 * Behaviour:
 * - Even if it is set to lazy, TWO SELECTS WILL BE PERFORMED since hibernate doesn't know if the
 * association exists or not => a proxy entity will be generated for association
 * - If we are sure that the association exists by executing SQL query ONLY ONE SELECT WILL BE PERFORMED
 * and a proxy object will be generated for association
 */
public class OneToOneTest extends BaseTest {

    Long userId;

    @BeforeEach
    public void init() {

        User user = new User();
        entityManager.persist(user);

        UserDetails userDetails = new UserDetails();
        userDetails.setUser(user);
        entityManager.persist(userDetails);

        entityManager.flush();

        userId = user.getId();
    }

    @Test
    public void shouldSave() {
        User user = new User("razvan.sebastian");
        entityManager.persist(user);

        UserDetails userDetails = UserDetails.builder()
                .fullName("Razvan Sebastian Parautiu")
                .user(user)
                .build();
        entityManager.persist(userDetails);

        entityManager.flush();

        assertEquals(user.getId(), userDetails.getId());
    }

    @Test
    public void shouldGetUserDetailsByUserIdFindApproach() {
        entityManager.clear();
        UserDetails userDetails = entityManager.find(UserDetails.class, userId);

        assertNotNull(userDetails);
    }

    @Test
    public void shouldGetUserDetailsByUserIdSqlApproach() {
        entityManager.clear();
        UserDetails userDetails = (UserDetails) entityManager.createQuery("SELECT ud FROM UserDetails ud WHERE ud.id = :id")
                .setParameter("id", userId)
                .getSingleResult();

        assertNotNull(userDetails);
    }

    @Test
    public void shouldRemoveUser() {
        entityManager.clear();
        UserDetails userDetails = entityManager.find(UserDetails.class, userId);
        entityManager.remove(userDetails);
        entityManager.remove(userDetails.getUser());

        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(User.class, userId));
        assertNull(entityManager.find(UserDetails.class, userId));
    }

    public void shouldRemoveUserDetailsForAUser() {
        entityManager.createQuery("DELETE FROM UserDetails ud WHERE ud.user.id = :id")
                .setParameter("id", userId)
                .executeUpdate();
        entityManager.clear();

        assertNull(entityManager.find(UserDetails.class, userId));
    }
}
