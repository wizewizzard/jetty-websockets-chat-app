package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.User;
import com.wu.chatserver.domain.UsersChatSession;
import com.wu.chatserver.service.ChatRoomService;
import com.wu.chatserver.service.MessageService;
import com.wu.chatserver.service.UserService;
import com.wu.chatserver.service.chatting.event.ChatRoomCreated;
import com.wu.chatserver.service.chatting.event.UserJoinedChatRoom;
import com.wu.chatserver.service.chatting.event.UserLeftChatRoom;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.enterprise.event.Event;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Slf4j
class WsChatRoomRealmEventsTest {
    private WsChatRoomRealm realm;
    private UserService userService;
    private ChatRoomService chatRoomService;
    private MessageService messageService;
    private ConnectionPool connectionPool;
    private ExecutorService executorService;

    @BeforeEach
    public void setUp(){
        Properties properties = new Properties();
        properties.setProperty("RoomUpTime", "5");
        userService = Mockito.mock(UserService.class);
        chatRoomService = Mockito.mock(ChatRoomService.class);
        messageService = Mockito.mock(MessageService.class);
        connectionPool = new ConnectionPool(userService);
        realm = new WsChatRoomRealm(connectionPool, userService, chatRoomService, messageService);
        realm.init(properties);
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    void shouldSetUserOnlineForNewlyCreatedRoom() {
        String userName = "TestUser";
        Long userId = 1L;
        String chatRoomName = "Test room #1";
        Long chatRoomId = 11L;

        User user = mock(User.class);
        ChatRoom chatRoomDomain = mock(ChatRoom.class);
        when(user.getUserName()).thenReturn(userName);
        when(user.getId()).thenReturn(userId);
        when(chatRoomDomain.getName()).thenReturn(chatRoomName);
        when(chatRoomDomain.getId()).thenReturn(chatRoomId);
        when(chatRoomDomain.getMembers()).thenReturn(Set.of(user));

        Mockito.when(userService.getUserByUserName(userName)).thenReturn(Optional.of(user));
        Mockito.when(chatRoomService.findChatRoomsForUser(user.getUserName())).thenReturn(List.of());

        ChatRoomCreated chatRoomCreated  = mock(ChatRoomCreated.class);
        when(chatRoomCreated.getChatRoom()).thenReturn(chatRoomDomain);
        when(chatRoomCreated.getCreatedBy()).thenReturn(user);

        Phaser phaser = new Phaser(2);

        Runnable client = () -> {
            try {
                ChatClientAPI api = new ChatClientAPI(realm);
                log.info("Client connecting...");
                api.connect(() -> userName);
                phaser.arriveAndAwaitAdvance();
                phaser.arriveAndAwaitAdvance();
                log.info("Client sending messages...");
                api.sendMessage(new Message(chatRoomDomain.getId(),"Hello!"));
                phaser.arriveAndAwaitAdvance();
                log.info("Client disconnecting...");
                api.disconnect();
                phaser.arriveAndAwaitAdvance();
            } catch (InterruptedException | TimeoutException e) {
                fail("Exception fired", e);
                e.printStackTrace();
            }

        };

        //WHEN
        executorService.submit(client);
        executorService.shutdown();

        try{
            log.info("Connection stage");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(0, 1000, TimeUnit.MILLISECONDS);
            log.info("Fire event stage");
            when(chatRoomService.findChatRoomWithMembersById(chatRoomDomain.getId())).thenReturn(Optional.of(chatRoomDomain));
            realm.chatRoomCreated(chatRoomCreated);
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(1, 1000, TimeUnit.MILLISECONDS);
            log.info("Send message stage");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(2, 1000, TimeUnit.MILLISECONDS);
            log.info("Disconnection stage");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(3, 1000, TimeUnit.MILLISECONDS);
        }
        catch (Exception e){
            fail("Exception fired", e);
            e.printStackTrace();
        }

        verify(userService, times(1))
                .setUserOnlineStatusForRoom(chatRoomDomain.getId(), user.getId(), UsersChatSession.OnlineStatus.ONLINE);
    }

    @Test
    void shouldSetJoinedUserStatusToOnline() {
        String userName = "TestUser";
        Long userId = 1L;
        String chatRoomName = "Test room #1";
        Long chatRoomId = 11L;

        User user = mock(User.class);
        ChatRoom chatRoomDomain = mock(ChatRoom.class);
        when(user.getUserName()).thenReturn(userName);
        when(user.getId()).thenReturn(userId);
        when(chatRoomDomain.getName()).thenReturn(chatRoomName);
        when(chatRoomDomain.getId()).thenReturn(chatRoomId);
        when(chatRoomDomain.getMembers()).thenReturn(Set.of());

        Mockito.when(userService.getUserByUserName(userName)).thenReturn(Optional.of(user));
        Mockito.when(chatRoomService.findChatRoomsForUser(user.getUserName())).thenReturn(List.of());

        UserJoinedChatRoom userJoinedChatRoom  = mock(UserJoinedChatRoom.class);
        when(userJoinedChatRoom.getChatRoom()).thenReturn(chatRoomDomain);
        when(userJoinedChatRoom.getUser()).thenReturn(user);

        Phaser phaser = new Phaser(2);

        Runnable client = () -> {
            try {
                ChatClientAPI api = new ChatClientAPI(realm);
                log.info("Client connecting...");
                api.connect(() -> userName);
                phaser.arriveAndAwaitAdvance();
                phaser.arriveAndAwaitAdvance();
                log.info("Client sending messages...");
                api.sendMessage(new Message(chatRoomDomain.getId(),"Hello!"));
                phaser.arriveAndAwaitAdvance();
                log.info("Client disconnecting...");
                api.disconnect();
                phaser.arriveAndAwaitAdvance();
            } catch (Exception e) {
                fail("Exception fired", e);
                e.printStackTrace();
            }

        };

        //WHEN
        executorService.submit(client);
        executorService.shutdown();

        try{
            log.info("Connection stage");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(0, 1000, TimeUnit.MILLISECONDS);
            log.info("Fire event stage");
            when(chatRoomDomain.getMembers()).thenReturn(Set.of(user));
            when(chatRoomService.findChatRoomWithMembersById(chatRoomDomain.getId())).thenReturn(Optional.of(chatRoomDomain));
            realm.userJoinedChatRoom(userJoinedChatRoom);
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(1, 1000, TimeUnit.MILLISECONDS);
            log.info("Send message stage");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(2, 1000, TimeUnit.MILLISECONDS);
            log.info("Disconnection stage");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(3, 1000, TimeUnit.MILLISECONDS);
        }
        catch (Exception e){
            fail("Exception fired", e);
            e.printStackTrace();
        }

        verify(userService, times(1))
                .setUserOnlineStatusForRoom(chatRoomDomain.getId(), user.getId(), UsersChatSession.OnlineStatus.ONLINE);
    }

    /**
     * Should NOT Set Joined User Status To Online because user has no connections opened
     */
    @Test
    void shouldNOTSetJoinedUserStatusToOnline() {
        String userName = "TestUser";
        Long userId = 1L;
        String chatRoomName = "Test room #1";
        Long chatRoomId = 11L;

        User user = mock(User.class);
        ChatRoom chatRoomDomain = mock(ChatRoom.class);
        when(user.getUserName()).thenReturn(userName);
        when(user.getId()).thenReturn(userId);
        when(chatRoomDomain.getName()).thenReturn(chatRoomName);
        when(chatRoomDomain.getId()).thenReturn(chatRoomId);
        when(chatRoomDomain.getMembers()).thenReturn(Set.of());

        Mockito.when(userService.getUserByUserName(userName)).thenReturn(Optional.of(user));
        Mockito.when(chatRoomService.findChatRoomsForUser(user.getUserName())).thenReturn(List.of());

        UserJoinedChatRoom userJoinedChatRoom  = mock(UserJoinedChatRoom.class);
        when(userJoinedChatRoom.getChatRoom()).thenReturn(chatRoomDomain);
        when(userJoinedChatRoom.getUser()).thenReturn(user);

        try{
            when(chatRoomDomain.getMembers()).thenReturn(Set.of(user));
            when(chatRoomService.findChatRoomWithMembersById(chatRoomDomain.getId())).thenReturn(Optional.of(chatRoomDomain));
            realm.userJoinedChatRoom(userJoinedChatRoom);
        }
        catch (Exception e){
            fail("Exception fired", e);
            e.printStackTrace();
        }

        verify(userService, times(0))
                .setUserOnlineStatusForRoom(chatRoomDomain.getId(), user.getId(), UsersChatSession.OnlineStatus.ONLINE);
    }

    @Test
    public void shouldSetJoinedUserStatusToOnlineAndOfflineWhenLeavesRoom(){
        String userName = "TestUser";
        Long userId = 1L;
        String chatRoomName = "Test room #1";
        Long chatRoomId = 11L;

        User user = mock(User.class);
        ChatRoom chatRoomDomain = mock(ChatRoom.class);
        when(user.getUserName()).thenReturn(userName);
        when(user.getId()).thenReturn(userId);
        when(chatRoomDomain.getName()).thenReturn(chatRoomName);
        when(chatRoomDomain.getId()).thenReturn(chatRoomId);
        when(chatRoomDomain.getMembers()).thenReturn(Set.of());

        Mockito.when(userService.getUserByUserName(userName)).thenReturn(Optional.of(user));
        Mockito.when(chatRoomService.findChatRoomsForUser(user.getUserName())).thenReturn(List.of());

        UserJoinedChatRoom userJoinedChatRoom  = mock(UserJoinedChatRoom.class);
        when(userJoinedChatRoom.getChatRoom()).thenReturn(chatRoomDomain);
        when(userJoinedChatRoom.getUser()).thenReturn(user);
        UserLeftChatRoom userLeftChatRoom = mock(UserLeftChatRoom.class);
        when(userLeftChatRoom.getChatRoom()).thenReturn(chatRoomDomain);
        when(userLeftChatRoom.getUser()).thenReturn(user);

        Phaser phaser = new Phaser(2);

        Runnable client = () -> {
            try {
                phaser.arriveAndAwaitAdvance();
                ChatClientAPI api = new ChatClientAPI(realm);
                log.info("Client connecting...");
                api.connect(() -> userName);
                phaser.arriveAndAwaitAdvance();
                phaser.arriveAndAwaitAdvance();
                log.info("Client sending messages...");
                api.sendMessage(new Message(chatRoomDomain.getId(),"Hello!"));
                phaser.arriveAndAwaitAdvance();
                phaser.arriveAndAwaitAdvance();
                log.info("Client disconnecting...");
                api.disconnect();
                phaser.arriveAndAwaitAdvance();
            } catch (Exception e) {
                fail("Exception fired", e);
                e.printStackTrace();
            }

        };

        //WHEN
        executorService.submit(client);
        executorService.shutdown();

        try{
            log.info("Connection stage starts:");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(0, 1000, TimeUnit.MILLISECONDS);
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(1, 1000, TimeUnit.MILLISECONDS);
            log.info("Fire event stage starts:");
            when(chatRoomDomain.getMembers()).thenReturn(Set.of(user));
            when(chatRoomService.findChatRoomWithMembersById(chatRoomDomain.getId())).thenReturn(Optional.of(chatRoomDomain));
            realm.userJoinedChatRoom(userJoinedChatRoom);
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(2, 1000, TimeUnit.MILLISECONDS);
            log.info("Send message stage starts:");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(3, 1000, TimeUnit.MILLISECONDS);
            log.info("User leaves chat room stage starts:");
            when(chatRoomDomain.getMembers()).thenReturn(Set.of());
            when(chatRoomService.findChatRoomWithMembersById(chatRoomDomain.getId())).thenReturn(Optional.of(chatRoomDomain));
            realm.userLeftChatRoom(userLeftChatRoom);
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(4, 1000, TimeUnit.MILLISECONDS);
            log.info("User disconnects stage starts:");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(4, 1000, TimeUnit.MILLISECONDS);
        }
        catch (Exception e){
            fail("Exception fired", e);
            e.printStackTrace();
        }

        verify(userService, times(1))
                .setUserOnlineStatusForRoom(chatRoomDomain.getId(), user.getId(), UsersChatSession.OnlineStatus.ONLINE);
        verify(userService, times(1))
                .setUserOnlineStatusForRoom(chatRoomDomain.getId(), user.getId(), UsersChatSession.OnlineStatus.OFFLINE);
    }
}