package com.wu.chatserver.repository;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.Message;
import com.wu.chatserver.repository.util.TestData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

class MessageRepositoryTest {
    static EntityManagerFactory emf;
    private MessageRepository messageRepositoryUT = new MessageRepository();

    private static final TestData testData = new TestData();

    @BeforeAll
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("chat_persistence_unit");
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            testData.getUsers().forEach(em::persist);
            testData.getChatRooms().forEach(em::persist);
            testData.getMessages().forEach(em::persist);
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
            testData.getMessages().stream().map(em::merge).forEach(em::remove);
            testData.getChatRooms().stream().map(em::merge).forEach(em::remove);
            testData.getUsers().stream().map(em::merge).forEach(em::remove);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Something went wrong", e);
        }
        emf.close();
    }

    @Test
    void findByChatBeforeDateTest() {
        //pre
        List<ChatRoom> chatRooms = testData.getChatRooms();
        assertThat(chatRooms).hasSizeGreaterThan(0);
        ChatRoom chatRoom = chatRooms.get(0);
        List<Message> messagesInTheChatRoom = testData.getMessages().stream().filter(m -> m.getChatRoom().equals(chatRoom)).collect(Collectors.toList());
        assertThat(messagesInTheChatRoom).hasSizeGreaterThanOrEqualTo(2);

        messageRepositoryUT.setEntityManager(emf.createEntityManager());

        List<Message> messages = messageRepositoryUT.findByChatBeforeDate(chatRoom.getId(),
                LocalDateTime.parse("2022-07-18T11:00:00Z", DateTimeFormatter.ISO_DATE_TIME),
                10);

        assertThat(messages).hasSize(1);
    }
}