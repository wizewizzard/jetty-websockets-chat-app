package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.User;
import com.wu.chatserver.service.ChatRoomService;
import com.wu.chatserver.service.MessageService;
import com.wu.chatserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.fail;

@Slf4j
class WsChatRoomRealmTest {

    private WsChatRoomRealm realm;
    private UserService userService;
    private ChatRoomService chatRoomService;
    private MessageService messageService;

    private ExecutorService executorService;
    @BeforeEach
    public void setUp(){
        userService = Mockito.mock(UserService.class);
        chatRoomService = Mockito.mock(ChatRoomService.class);
        messageService = Mockito.mock(MessageService.class);
        realm = new WsChatRoomRealm(userService, chatRoomService, messageService);
        executorService = Executors.newCachedThreadPool();
    }

    @Test
    public void shouldConnectAndDisconnectUser() throws InterruptedException, BrokenBarrierException {
        String userName1 = "testUser1";
        String userName2 = "testUser2";
        User user1 = Mockito.mock(User.class);
        Mockito.when(user1.getUserName()).thenReturn(userName1);
        User user2 = Mockito.mock(User.class);
        Mockito.when(user2.getUserName()).thenReturn(userName2);
        ChatRoom chatRoomDomain1 = Mockito.mock(ChatRoom.class);
        ChatRoom chatRoomDomain2 = Mockito.mock(ChatRoom.class);
        Mockito.when(chatRoomDomain1.getId()).thenReturn(1L);
        Mockito.when(chatRoomDomain1.getName()).thenReturn("Test chat room #1");
        Mockito.when(chatRoomDomain1.getMembers()).thenReturn(Set.of(user1, user2));
        Mockito.when(chatRoomDomain2.getId()).thenReturn(2L);
        Mockito.when(chatRoomDomain2.getName()).thenReturn("Test chat room #2");

        Mockito.when(userService.getUserByUserName(userName1)).thenReturn(Optional.of(user1));
        Mockito.when(userService.getUserByUserName(userName2)).thenReturn(Optional.of(user2));
        Mockito.when(user1.getChatRooms()).thenReturn(List.of(chatRoomDomain1, chatRoomDomain2));
        Mockito.when(user2.getChatRooms()).thenReturn(List.of(chatRoomDomain2));

        final Phaser phaser = new Phaser(3);
        //CyclicBarrier barrier = new CyclicBarrier(3);

        Runnable client1 = () -> {
            log.info("User thread {} has started", userName1);
            try {
                ChatClientAPI api = realm.tryConnect(() -> userName1);
                log.info("User {} has connected", userName1);
                phaser.arriveAndAwaitAdvance();
                api.sendMessage(new Message(chatRoomDomain2.getId(), "Test message from user 1"));
                Thread.sleep(10);
                phaser.arriveAndAwaitAdvance();
                api.disconnect();
                phaser.arriveAndDeregister();
            }
            catch (Exception e) {
                fail("Test was poorly built");
                throw new RuntimeException(e);
            }

        };

        Runnable client2 = () -> {
            log.info("User thread {} has started", userName2);
            try {
                ChatClientAPI api = realm.tryConnect(() -> userName2);
                log.info("User {} has connected", userName2);
                phaser.arriveAndAwaitAdvance();
                while(true){
                    Message message = api.pollMessage();
                    System.out.println("Received: " + message);
                    if(message.getBody().equals("Test message from user 1")){
                        phaser.arriveAndAwaitAdvance();
                    }

                }
            } catch (Exception e) {
                fail("Test was poorly built");
                throw new RuntimeException(e);
            }

        };

        executorService.submit(client2);
        executorService.submit(client1);
        System.out.println(phaser.getPhase());
        try{
            log.info("Connection phase");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(0, 1000, TimeUnit.MILLISECONDS);
            System.out.println(phaser.getPhase());
            log.info("Message phase");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(1, 1000, TimeUnit.MILLISECONDS);
            System.out.println(phaser.getPhase());
            log.info("Disconnect phase");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(2, 1000, TimeUnit.MILLISECONDS);
            System.out.println(phaser.getPhase());
        }
        catch (InterruptedException | TimeoutException e){
            fail("Timeout reached");
            e.printStackTrace();
        }
    }

    @Test
    public void testPhaser(){
        final Phaser phaser = new Phaser(3);

        Runnable r = () -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("A");
            phaser.arriveAndAwaitAdvance();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("B");
            phaser.arriveAndAwaitAdvance();

        };

        executorService.submit(r);
        executorService.submit(r);
        try{
            System.out.println("Waiting");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(0, 1000, TimeUnit.MILLISECONDS);

            System.out.println("Waiting");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(1, 1000, TimeUnit.MILLISECONDS);

        }
        catch (TimeoutException | InterruptedException exception){
            fail("Meh");
        }
    }
}