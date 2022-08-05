package com.wu.chatserver.repository;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.User;
import com.wu.chatserver.repository.util.TestData;
import org.junit.jupiter.api.*;

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

    private EntityManager em;

    @BeforeAll
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("chat_persistence_unit");
        EntityManager em = emf.createEntityManager();

        try {
            em.getTransaction().begin();
            testData.getUsers().forEach(em::persist);
            testData.getChatRooms().forEach(em::persist);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Something went wrong", e);

        }
    }

    @AfterAll
    public static void tearDown() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            testData.getChatRooms().stream().map(em::merge).forEach(r -> {
                em.refresh(r);
                em.remove(r);
            });
            testData.getUsers().stream().map(em::merge).forEach(r -> {
                em.refresh(r);
                em.remove(r);
            });
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Something went wrong", e);

        }
        emf.close();
    }

    @BeforeEach
    public void beforeEach() {
        em = emf.createEntityManager();
        userRepositoryUT = new UserRepository();
        userRepositoryUT.setEntityManager(em);
    }

    @AfterEach
    public void afterEach() {
        if (em != null)
            em.close();
        em = null;
    }

    @Test
    void findUserByUserNameTest() {
        List<User> users = testData.getUsers();
        assertThat(users).hasSizeGreaterThan(0);
        String name = users.get(0).getUserName();
        String notExistingName = UUID.randomUUID().toString();

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

        assertThat(userRepositoryUT.uniqueUserNameAndEmail(notUniqueName, "random")).isFalse();
        assertThat(userRepositoryUT.uniqueUserNameAndEmail("random", notUniqueEmail)).isFalse();
        assertThat(userRepositoryUT.uniqueUserNameAndEmail(notUniqueName, notUniqueEmail)).isFalse();
        assertThat(userRepositoryUT.uniqueUserNameAndEmail("random", "random")).isTrue();

        em.close();
    }

    @Test
    void shouldFetchUserWithChatRoomsInOneQuery() {
        User user = testData.getUsers().get(0);
        assertThat(user.getChatRooms()).hasSizeGreaterThan(0);
        List<ChatRoom> usersChatRooms = user.getChatRooms();

        Optional<User> optionalUser = userRepositoryUT.findUserWithChatRoomsByUserName(user.getUserName());

        assertThat(optionalUser)
                .isPresent()
                .get()
                .satisfies(userFetched -> {
                    assertThat(userFetched.getChatRooms()).containsAll(usersChatRooms);
        });
    }

    @Test
    void findUserWithChatRoomsByUserName() {
        List<User> users = testData.getUsers();
        assertThat(users).hasSizeGreaterThan(0);
        User julia = users.stream().filter(u -> u.getUserName().equals("Julia")).findFirst().orElseThrow();
        User harry = users.stream().filter(u -> u.getUserName().equals("Harry")).findFirst().orElseThrow();
        User denny = users.stream().filter(u -> u.getUserName().equals("Denny")).findFirst().orElseThrow();
        assertThat(harry.getChatRooms()).hasSizeGreaterThan(0);
        assertThat(julia.getChatRooms()).hasSizeGreaterThan(0);
        assertThat(denny.getChatRooms()).hasSize(0);

        Optional<User> juliaOptional = userRepositoryUT.findUserWithChatRoomsByUserName(julia.getUserName());
        Optional<User> harryOptional = userRepositoryUT.findUserWithChatRoomsByUserName(harry.getUserName());
        Optional<User> dennyOptional = userRepositoryUT.findUserWithChatRoomsByUserName(denny.getUserName());

        assertThat(juliaOptional).isPresent()
                .get()
                .satisfies(u ->
                {
                    assertThat(u.getChatRooms()).hasSize(julia.getChatRooms().size());
                    assertThat(u.getChatRooms()).containsAll(julia.getChatRooms());
                });
        assertThat(harryOptional).isPresent()
                .get()
                .satisfies(u ->
                {
                    assertThat(u.getChatRooms()).hasSize(harry.getChatRooms().size());
                    assertThat(u.getChatRooms()).containsAll(harry.getChatRooms());
                });
        assertThat(dennyOptional).isPresent()
                .get()
                .satisfies(u ->
                {
                    assertThat(u.getChatRooms()).hasSize(denny.getChatRooms().size());
                    assertThat(u.getChatRooms()).containsAll(denny.getChatRooms());
                });

    }
}