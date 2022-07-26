package com.wu.chatserver.service;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.Message;
import com.wu.chatserver.domain.User;
import com.wu.chatserver.dto.MessageDTO;
import com.wu.chatserver.repository.ChatRoomRepository;
import com.wu.chatserver.repository.MessageRepository;
import com.wu.chatserver.repository.UserRepository;
import com.wu.chatserver.repository.util.TestData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class MessageServiceImplTest {

    static EntityManagerFactory emf;
    MessageServiceImpl messageServiceUT;
    private EntityManager em;
    private ChatRoomRepository chatRoomRepository;
    private UserRepository userRepository;
    private MessageRepository messageRepository;
    private static final TestData testData = new TestData();

    @BeforeAll
    public static void setUp(){
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
        finally {
            em.close();
        }
    }

    @AfterAll
    public static void tearDown(){
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
        finally {
            em.close();
        }
        emf.close();
    }

    @BeforeEach
    public void beforeEach(){
        em = emf.createEntityManager();
        chatRoomRepository = Mockito.spy(new ChatRoomRepository());
        userRepository = new UserRepository();
        messageRepository = Mockito.spy(new MessageRepository());
        chatRoomRepository.setEntityManager(em);
        userRepository.setEntityManager(em);
        messageRepository.setEntityManager(em);
        messageServiceUT = new MessageServiceImpl(chatRoomRepository, userRepository, messageRepository, em);
    }

    @AfterEach
    public void afterEach(){
        if(em != null)
            em.close();
        em = null;
    }

    /**
     * Get user and chat room. Assert that user is a member of the room and publish a message on behalf of the user
     */
    @Test
    void shouldAddMessageToTheChatAndHistoryChanges() {
        ChatRoom chatRoom = testData.getChatRooms().stream().filter(r -> r.getName().equals("Julia's chat")).findFirst().orElseThrow();
        List<User> members = new ArrayList<>(chatRoom.getMembers());
        assertThat(members).hasSizeGreaterThan(0);
        String messageBody = "shouldAddMessageToTheChat is testing you";
        ArgumentCaptor<ChatRoom> chatRoomCaptor = ArgumentCaptor.forClass(ChatRoom.class);

        List<MessageDTO.Response.MessageWithAuthor> messageHistoryBefore =
                messageServiceUT.getMessageHistory(chatRoom.getId(), LocalDateTime.now(), 10);

        messageServiceUT.publishMessage(chatRoom.getId(), members.get(0).getId(), messageBody);
        em.clear();

        List<MessageDTO.Response.MessageWithAuthor> messageHistoryAfter =
                messageServiceUT.getMessageHistory(chatRoom.getId(), LocalDateTime.now(), 10);

        Mockito.verify(chatRoomRepository, Mockito.times(1)).save(chatRoomCaptor.capture());

        assertThat(messageHistoryAfter).hasSizeGreaterThan(messageHistoryBefore.size());
        messageHistoryAfter.removeAll(messageHistoryBefore);
        assertThat(messageHistoryAfter).hasSize(1);
        assertThat(messageHistoryAfter.get(0)).satisfies(m -> {
            assertThat(m.getPublishedAt()).isNotNull();
            assertThat(m.getBody()).isEqualTo(messageBody);
            assertThat(m.getCreatedBy()).isEqualTo(members.get(0).getUserName());
        });
    }

    @Test
    void getMessageHistory() {
    }
}