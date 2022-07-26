package com.wu.chatserver.repository;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.Message;
import com.wu.chatserver.dto.MessageDTO;
import com.wu.chatserver.repository.util.TestData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    void findMessagesByChatBeforeDateTest() {
        int pageSize = 10;
        List<ChatRoom> chatRooms = testData.getChatRooms();
        assertThat(chatRooms).hasSizeGreaterThan(0);
        ChatRoom chatRoom = chatRooms.stream().filter(cr -> cr.getName().equals("Julia's chat")).findFirst().orElseThrow();

        List<Message> messagesInTheChatRoom = chatRoom.getMessages()
                .stream()
                .sorted(Comparator.comparing(Message::getPublishedAt).reversed())
                .collect(Collectors.toList());
        assertThat(messagesInTheChatRoom).hasSizeGreaterThan(0);

        messageRepositoryUT.setEntityManager(emf.createEntityManager());

        List<MessageDTO.Response.MessageWithAuthor> messagesDto = messageRepositoryUT.findByChatBeforeDate(chatRoom,
                messagesInTheChatRoom.get(0).getPublishedAt().plusMinutes(1),
                pageSize);

        assertThat(messagesDto).hasSize(messagesInTheChatRoom.size());
        //new messages first
        assertThat(messagesDto).isSortedAccordingTo(Comparator.comparing(MessageDTO.Response.MessageWithAuthor::getPublishedAt).reversed());
        for (int i = 0; i < messagesInTheChatRoom.size(); i++) {
            assertThat(messagesDto.get(i).getCreatedBy()).isEqualTo(messagesInTheChatRoom.get(i).getCreatedBy().getUserName());
            assertThat(messagesDto.get(i).getBody()).isEqualTo(messagesInTheChatRoom.get(i).getBody());
            assertThat(messagesDto.get(i).getPublishedAt()).isEqualTo(messagesInTheChatRoom.get(i).getPublishedAt());
        }
    }

    @Test
    void shouldReturnListOfGivenPageSize() {
        int pageSize = 2;
        List<ChatRoom> chatRooms = testData.getChatRooms();
        assertThat(chatRooms).hasSizeGreaterThan(0);
        ChatRoom chatRoom = chatRooms.get(0);

        List<Message> messagesInTheChatRoom = chatRoom.getMessages()
                .stream()
                .sorted(Comparator.comparing(Message::getPublishedAt).reversed())
                .collect(Collectors.toList());
        assertThat(messagesInTheChatRoom).hasSizeGreaterThan(0);

        messageRepositoryUT.setEntityManager(emf.createEntityManager());

        List<MessageDTO.Response.MessageWithAuthor> messagesDto = messageRepositoryUT.findByChatBeforeDate(chatRoom,
                messagesInTheChatRoom.get(0).getPublishedAt().plusMinutes(1),
                pageSize);

        assertThat(messagesDto).hasSize(pageSize);
        //from the old ones to the new messagesDto
        assertThat(messagesDto).isSortedAccordingTo(Comparator.comparing(MessageDTO.Response.MessageWithAuthor::getPublishedAt).reversed());
        for (int i = 0; i < pageSize; i++) {
            assertThat(messagesDto.get(i).getCreatedBy()).isEqualTo(messagesInTheChatRoom.get(i).getCreatedBy().getUserName());
            assertThat(messagesDto.get(i).getBody()).isEqualTo(messagesInTheChatRoom.get(i).getBody());
            assertThat(messagesDto.get(i).getPublishedAt()).isEqualTo(messagesInTheChatRoom.get(i).getPublishedAt());
        }
    }

    @Test
    void shouldReturnNoMessagesAsThereIsNoMessagesBeforeGivenDate() {
        //pre
        List<ChatRoom> chatRooms = testData.getChatRooms();
        assertThat(chatRooms).hasSizeGreaterThan(0);
        ChatRoom chatRoom = chatRooms.get(0);

        List<Message> messagesInTheChatRoom = chatRoom.getMessages()
                .stream()
                .sorted(Comparator.comparing(Message::getPublishedAt).reversed())
                .collect(Collectors.toList());
        assertThat(messagesInTheChatRoom).hasSizeGreaterThan(0);

        messageRepositoryUT.setEntityManager(emf.createEntityManager());

        List<MessageDTO.Response.MessageWithAuthor> messagesDto = messageRepositoryUT.findByChatBeforeDate(chatRoom,
                messagesInTheChatRoom.get(messagesInTheChatRoom.size() - 1).getPublishedAt().minusMinutes(1),
                10);

        assertThat(messagesDto).hasSize(0);
    }

    @Test
    void shouldReturnNoMessagesAsThereIsNoSpecifiedChatRoom() {
        //pre
        List<ChatRoom> chatRooms = testData.getChatRooms();
        assertThat(chatRooms).hasSizeGreaterThan(0);
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(9999L);

        messageRepositoryUT.setEntityManager(emf.createEntityManager());

        List<MessageDTO.Response.MessageWithAuthor> messagesDto = messageRepositoryUT.findByChatBeforeDate(chatRoom,
                LocalDateTime.now(),
                10);

        assertThat(messagesDto).hasSize(0);
    }
}