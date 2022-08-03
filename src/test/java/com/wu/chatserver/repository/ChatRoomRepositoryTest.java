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
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRoomRepositoryTest {

    static EntityManagerFactory emf;
    private ChatRoomRepository chatRoomRepositoryUT = new ChatRoomRepository();

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
        chatRoomRepositoryUT = new ChatRoomRepository();
        chatRoomRepositoryUT.setEntityManager(em);
    }

    @AfterEach
    public void afterEach() {
        if (em != null)
            em.close();
        em = null;
    }

    @Test
    void isUserMemberOfChatRoomTest() {
        List<User> users = testData.getUsers();
        List<ChatRoom> chatRooms = testData.getChatRooms();
        assertThat(chatRooms.get(0).getMembers()).contains(users.get(0));
        assertThat(chatRooms.get(0).getMembers()).doesNotContain(users.get(2));

        assertThat(chatRoomRepositoryUT.isUserMemberOfChatRoom(chatRooms.get(0), users.get(0)))
                .isTrue();
        assertThat(chatRoomRepositoryUT.isUserMemberOfChatRoom(chatRooms.get(0), users.get(2)))
                .isFalse();
    }

    @Test
    void shouldFetchChatRoomWithUsersInOneQuery() {
        ChatRoom chatRoom = testData.getChatRooms().get(0);
        Set<User> members = chatRoom.getMembers();

        Optional<ChatRoom> chatRoomOptional = chatRoomRepositoryUT.findChatRoomByIdWithMembers(chatRoom.getId());

        assertThat(chatRoomOptional)
                .isPresent()
                .get()
                .satisfies(chatRoomFetched -> {
                    assertThat(chatRoomFetched.getMembers()).containsAll(members);
                });
    }
}