package com.wu.chatserver.service;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.dto.ChatRoomDTO;
import com.wu.chatserver.repository.ChatRoomRepository;
import com.wu.chatserver.repository.UserRepository;
import com.wu.chatserver.repository.util.TestData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@Slf4j
class ChatRoomServiceImplTest {
    static EntityManagerFactory emf;
    ChatRoomServiceImpl chatRoomServiceUT;
    private EntityManager em;
    private ChatRoomRepository chatRoomRepository;
    private UserRepository userRepository;
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
        chatRoomRepository = new ChatRoomRepository();
        userRepository = new UserRepository();
        chatRoomRepository.setEntityManager(em);
        userRepository.setEntityManager(em);
        chatRoomServiceUT = new ChatRoomServiceImpl(chatRoomRepository, userRepository, em);
    }

    @AfterEach
    public void afterEach(){
        if(em != null)
            em.close();
        em = null;
    }

    @Test
    void shouldCreateFindAndDeleteChatRoom() {
        Long userId = 1L;
        ChatRoomDTO.Request.Create createDto = new ChatRoomDTO.Request.Create();
        createDto.setChatName("Chat with me");

        ChatRoom chatRoom = chatRoomServiceUT.createChatRoom(createDto, userId);

        assertThat(chatRoom.getId()).isNotNull();
        assertThat(chatRoom).satisfies(cr -> {
            assertThat(cr.getCreatedBy().getId()).isEqualTo(userId);
            assertThat(cr.getMembers()).hasSize(1);
            assertThat(cr.getName()).isEqualTo(createDto.getChatName());
        });
        log.info("----Created----");
        em.clear();
        Optional<ChatRoom> roomAfterSave = chatRoomServiceUT.findChatRoomById(chatRoom.getId());

        assertThat(roomAfterSave).isPresent();
        System.out.println("---------");
        assertThat(roomAfterSave).get().satisfies(cr -> {
            assertThat(cr.getCreatedBy().getId()).isEqualTo(userId);
            assertThat(cr.getMembers()).hasSize(1);
            assertThat(cr.getName()).isEqualTo(createDto.getChatName());
        });
        log.info("----Found----");
        em.clear();
        chatRoomServiceUT.deleteChatRoom(chatRoom.getId(), userId);

        Optional<ChatRoom> roomAfterDelete = chatRoomServiceUT.findChatRoomById(chatRoom.getId());

        assertThat(roomAfterDelete).isNotPresent();
        log.info("----Deleted----");
    }

    @Test
    void shouldAddAndRemoveUserFromChat() {
        Long creatorId = 1L;
        Long userId = 2L;
        ChatRoomDTO.Request.Create createDto = new ChatRoomDTO.Request.Create();
        createDto.setChatName("Chat with me");

        ChatRoom chatRoomCreated = chatRoomServiceUT.createChatRoom(createDto, creatorId);
        assertThat(chatRoomCreated.getId()).isNotNull();
        log.info("----Chat room created----");

        chatRoomServiceUT.addUserToChat(chatRoomCreated.getId(), userId );
        em.clear();
        Optional<ChatRoom> chatRoomSelected = chatRoomServiceUT.findChatRoomById(chatRoomCreated.getId());
        assertThat(chatRoomSelected).isPresent()
                .get().satisfies(cr -> {
                    assertThat(cr.getMembers()).hasSize(2);
                });
        log.info("----User added----");
        em.clear();
        chatRoomServiceUT.removeUserFromChat(chatRoomCreated.getId(), userId);

        chatRoomSelected = chatRoomServiceUT.findChatRoomById(chatRoomCreated.getId());
        assertThat(chatRoomSelected).isPresent()
                .get().satisfies(cr -> {
                    assertThat(cr.getMembers()).hasSize(1);
                });
        log.info("----User removed----");

        chatRoomServiceUT.deleteChatRoom(chatRoomCreated.getId(), creatorId);
    }

}