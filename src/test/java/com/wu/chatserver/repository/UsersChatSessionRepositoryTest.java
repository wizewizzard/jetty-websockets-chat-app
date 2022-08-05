package com.wu.chatserver.repository;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.User;
import com.wu.chatserver.domain.UsersChatSession;
import com.wu.chatserver.dto.UserDTO;
import com.wu.chatserver.repository.util.TestData;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class UsersChatSessionRepositoryTest {

    static EntityManagerFactory emf;
    private UsersChatSessionRepository chatSessionRepositoryUT;
    private ChatRoomRepository chatRoomRepository;
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
    public void beforeEach(){
        em = emf.createEntityManager();
        chatSessionRepositoryUT = new UsersChatSessionRepository();
        chatSessionRepositoryUT.setEntityManager(em);
        chatRoomRepository = new ChatRoomRepository();
        chatRoomRepository.setEntityManager(em);
    }

    @AfterEach
    public void afterEach(){
        if(em != null)
            em.close();
        em = null;
    }

    @Test
    void setOnlineStatus() {
        List<User> users = testData.getUsers();
        List<ChatRoom> chatRooms = testData.getChatRooms();
        ChatRoom chatRoom = chatRooms.get(0);
        LocalDateTime updateDT = LocalDateTime.parse("2022-07-18T14:00:00Z", DateTimeFormatter.ISO_DATE_TIME);
        User onlineUser = users.stream().filter(u -> u.getUserName().equals("Jack")).findFirst().orElseThrow();
        User offlineUser = users.stream().filter(u -> u.getUserName().equals("Julia")).findFirst().orElseThrow();
        User externalUser = users.stream().filter(u -> u.getUserName().equals("Denny")).findFirst().orElseThrow();
        List<UserDTO.Response.UserOnlineStatus> usersOnlineStatuses1 = chatSessionRepositoryUT.getUsersOnlineStatusForChatRoom(chatRoom);
        assertThat(chatRoom.getMembers()).contains(onlineUser);
        assertThat(chatRoom.getMembers()).contains(offlineUser);
        assertThat(chatRoom.getMembers()).doesNotContain(externalUser);

        log.info("Adding an external user");
        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            ChatRoom merged = em.merge(chatRoom);
            User mergedExternalUser = em.merge(externalUser);
            merged.addMember(mergedExternalUser);
            tx.commit();
            chatRoom = merged;
        }
        catch (Throwable t){
            tx.rollback();
            fail(t);
        }
        log.info("External user was added to the chat room");

        tx = em.getTransaction();
        try{
            tx.begin();
            chatSessionRepositoryUT.setOnlineStatus(chatRoom, onlineUser, UsersChatSession.OnlineStatus.OFFLINE, updateDT);
            chatSessionRepositoryUT.setOnlineStatus(chatRoom, offlineUser, UsersChatSession.OnlineStatus.ONLINE, updateDT);
            chatSessionRepositoryUT.setOnlineStatus(chatRoom, externalUser, UsersChatSession.OnlineStatus.ONLINE, updateDT);
            tx.commit();
        }
        catch (Throwable t){
            tx.rollback();
            fail(t);
        }

        em.clear();
        log.info("Querying members' online statuses");
        List<UserDTO.Response.UserOnlineStatus> usersOnlineStatuses = chatSessionRepositoryUT.getUsersOnlineStatusForChatRoom(chatRoom);

        assertThat(usersOnlineStatuses).hasSize(3);
        assertThat(usersOnlineStatuses).allSatisfy(userOnlineStatus -> {
            if(userOnlineStatus.getUserName().equals(offlineUser.getUserName())
                    || userOnlineStatus.getUserName().equals(externalUser.getUserName())){
                assertThat(userOnlineStatus.getStartedAt()).isEqualTo(updateDT);
                assertThat(userOnlineStatus.getEndedAt()).isNull();
            }
            else if(userOnlineStatus.getUserName().equals(onlineUser.getUserName())){
                assertThat(userOnlineStatus.getStartedAt()).isNotNull();
                assertThat(userOnlineStatus.getEndedAt()).isEqualTo(updateDT);
            }
            else
                fail("Dto is not valid");
        });
        //clean up
        try{
            tx.begin();
            ChatRoom merged = em.merge(chatRoom);
            User mergedExternalUser = em.merge(externalUser);
            merged.removeMember(mergedExternalUser);
            tx.commit();
        }
        catch (Throwable t){
            tx.rollback();
            fail(t);
        }
    }

    @Test
    void getUsersOnlineStatusForChatRoom() {
        List<ChatRoom> chatRooms = testData.getChatRooms();
        ChatRoom chatRoom = chatRooms.get(0);
        assertThat(chatRoom).isNotNull();

        List<UserDTO.Response.UserOnlineStatus> usersOnlineStatuses = chatSessionRepositoryUT.getUsersOnlineStatusForChatRoom(chatRoom);
        assertThat(usersOnlineStatuses).hasSize(chatRoom.getMembers().size());

    }

    @Test
    public void shouldSetUserOnlineAndOfflineInAllRoomsHeHasMembership(){
        List<User> users = testData.getUsers();
        List<ChatRoom> chatRooms = testData.getChatRooms();
        User juliaUser = users.stream().filter(u -> u.getUserName().equals("Jack")).findFirst().orElseThrow();
        List<ChatRoom> userRooms = juliaUser.getChatRooms();
        User offlineUser = users.stream().filter(u -> u.getUserName().equals("Julia")).findFirst().orElseThrow();

        EntityTransaction tx = em.getTransaction();
        try{
            tx.begin();
            chatSessionRepositoryUT.setUserOnline(juliaUser, LocalDateTime.now());
            tx.commit();
        }
        catch (Throwable t){
            tx.rollback();
            fail(t);
        }

        em.clear();
        userRooms.forEach(r -> {
            List<UserDTO.Response.UserOnlineStatus> usersOnlineStatuses = chatSessionRepositoryUT.getUsersOnlineStatusForChatRoom(r);
            assertThat(usersOnlineStatuses)
                    .anyMatch(os -> os.getUserName().equals(juliaUser.getUserName()) && os.getEndedAt() == null);
        });
        em.clear();

        tx = em.getTransaction();
        try{
            tx.begin();
            chatSessionRepositoryUT.setUserOffline(juliaUser, LocalDateTime.now());
            tx.commit();
        }
        catch (Throwable t){
            tx.rollback();
            fail(t);
        }

        userRooms.forEach(r -> {
            List<UserDTO.Response.UserOnlineStatus> usersOnlineStatuses = chatSessionRepositoryUT.getUsersOnlineStatusForChatRoom(r);
            assertThat(usersOnlineStatuses)
                    .anyMatch(os -> os.getUserName().equals(juliaUser.getUserName()) && os.getEndedAt() != null);
        });
    }
}