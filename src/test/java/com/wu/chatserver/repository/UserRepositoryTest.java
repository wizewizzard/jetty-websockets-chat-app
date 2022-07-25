package com.wu.chatserver.repository;

import com.wu.chatserver.domain.User;
import com.wu.chatserver.repository.util.TestData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryTest {
    static EntityManagerFactory emf;
    private UserRepository userRepositoryUT = new UserRepository();

    private static final TestData testData = new TestData();

    @BeforeAll
    public static void setUp(){
        emf = Persistence.createEntityManagerFactory("chat_persistence_unit");
        EntityManager em = emf.createEntityManager();

        try{
            em.getTransaction().begin();
            testData.getUsers().forEach(em::persist);
            em.getTransaction().commit();
        }
        catch (Exception e){
            em.getTransaction().rollback();
            throw new RuntimeException("Something went wrong", e);

        }
    }

    @AfterAll
    public static void tearDown() {
        EntityManager em = emf.createEntityManager();
        try{
            em.getTransaction().begin();
            testData.getUsers().stream().map(em::merge).forEach(em::remove);
            em.getTransaction().commit();
        }
        catch (Exception e){
            em.getTransaction().rollback();
            throw new RuntimeException("Something went wrong", e);

        }
        emf.close();
    }

    @Test
    void findUserByUserNameTest() {
        List<User> users = testData.getUsers();
        assertThat(users).hasSizeGreaterThan(0);
        String name = users.get(0).getUserName();
        String notExistingName = UUID.randomUUID().toString();
        EntityManager em = emf.createEntityManager();
        userRepositoryUT.setEntityManager(em);

        Optional<User> userOptional = userRepositoryUT.findUserByUserName(name);
        Optional<User> userForNotExistingNameOptional = userRepositoryUT.findUserByUserName(notExistingName);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getUserName()).isEqualTo(name);
                });
        assertThat(userForNotExistingNameOptional).isNotPresent();
        em.close();
    }

    @Test
    void uniqueUserNameAndEmailTest() {
        List<User> users = testData.getUsers();
        assertThat(users).hasSizeGreaterThan(0);
        String notUniqueName = users.get(0).getUserName();
        String notUniqueEmail = users.get(0).getEmail();

        EntityManager em = emf.createEntityManager();

        userRepositoryUT.setEntityManager(em);
        assertThat(userRepositoryUT.uniqueUserNameAndEmail(notUniqueName, "random")).isFalse();
        assertThat(userRepositoryUT.uniqueUserNameAndEmail("random", notUniqueEmail)).isFalse();
        assertThat(userRepositoryUT.uniqueUserNameAndEmail(notUniqueName, notUniqueEmail)).isFalse();
        assertThat(userRepositoryUT.uniqueUserNameAndEmail("random", "random")).isTrue();

        em.close();
    }

}