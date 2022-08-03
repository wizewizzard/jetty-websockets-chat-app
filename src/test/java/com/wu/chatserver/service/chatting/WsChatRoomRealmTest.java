package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.User;
import com.wu.chatserver.service.ChatRoomService;
import com.wu.chatserver.service.MessageService;
import com.wu.chatserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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
                ChatClientAPI api = new ChatClientAPI(realm);
                api.connect(() -> userName1);
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
                ChatClientAPI api = new ChatClientAPI(realm);
                api.connect(() -> userName2);
                log.info("User {} has connected", userName2);
                phaser.arriveAndAwaitAdvance();
                while(true){
                    Message message = api.pollMessage();
                    System.out.println("Received: " + message);
                    if(message.getBody().equals("Test message from user 1")){
                        phaser.arriveAndAwaitAdvance();
                    }
                    if(message.getBody().equals("User " + userName1 + " goes offline")){
                        phaser.arriveAndDeregister();
                    }

                }
            } catch (Exception e) {
                fail("Test was poorly built");
                throw new RuntimeException(e);
            }

        };

        executorService.submit(client2);
        executorService.submit(client1);
        executorService.shutdown();
        System.out.println(phaser.getPhase());
        try{
            log.info("Connection phase");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(0, 1000, TimeUnit.MILLISECONDS);
            log.info("Message phase");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(1, 1000, TimeUnit.MILLISECONDS);
            log.info("Disconnect phase");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(2, 1000, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException | TimeoutException e){
            fail("Timeout reached");
            e.printStackTrace();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 3})
    public void testMessageExchange(int clientsNum){
        //GIVEN
        assertThat(clientsNum).isGreaterThanOrEqualTo(0);
        final List<Callable<List<Message>>> clients = new ArrayList<>();
        final List<User> mockedUsers = new ArrayList<>();
        final Phaser phaser = new Phaser(clientsNum + 1);
        final ChatRoom chatRoomDomain = Mockito.mock(ChatRoom.class);
        Mockito.when(chatRoomDomain.getId()).thenReturn(1L);
        Mockito.when(chatRoomDomain.getName()).thenReturn("Test chat room #1");

        List<String> userNames = Stream.generate(() -> UUID.randomUUID().toString())
                .limit(clientsNum)
                .collect(Collectors.toList());
        userNames.forEach(userName -> {
            User user = Mockito.mock(User.class);
            Mockito.when(user.getUserName()).thenReturn(userName);
            Mockito.when(user.getId()).thenReturn(ThreadLocalRandom.current().nextLong());
            Mockito.when(userService.getUserByUserName(userName)).thenReturn(Optional.of(user));
            Mockito.when(user.getChatRooms()).thenReturn(List.of(chatRoomDomain));

            mockedUsers.add(user);
            clients.add(() -> {
                ChatClientAPI api = new ChatClientAPI(realm);
                api.connect(() -> userName);
                List<Message> messagesReceived = new ArrayList<>();
                log.info("User {} connected", userName);
                Thread messageReceivingThread = new Thread(() -> {
                    log.info("Message receiving thread for user {} started", userName);
                    try{
                    while(!Thread.interrupted()){
                            Message message = api.pollMessage();
                            log.info("Received message {}", message);
                            messagesReceived.add(message);
                        }
                    }
                    catch (InterruptedException ignored){
                    }
                    log.info("Message receiving thread for user {} is over", userName);
                });
                messageReceivingThread.start();
                phaser.arriveAndAwaitAdvance();
                api.sendMessage(new Message(chatRoomDomain.getId(),"Hello!"));
                api.sendMessage(new Message(chatRoomDomain.getId(),"My name is " + userName));
                phaser.arriveAndAwaitAdvance();
                Thread.sleep(500);
                messageReceivingThread.interrupt();
                phaser.arriveAndDeregister();
                return messagesReceived;
            });
        });
        Mockito.when(chatRoomDomain.getMembers()).thenReturn(new HashSet<>(mockedUsers));

        //WHEN
        List<Future<List<Message>>> messageFutures = clients
                .stream()
                .map(executorService::submit)
                .collect(Collectors.toList());
        executorService.shutdown();

        //THEN
        try{
            log.info("Connection phase");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(0, 1000, TimeUnit.MILLISECONDS);
            log.info("Message phase");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(1, 1000, TimeUnit.MILLISECONDS);
            log.info("Disconnect phase");
            phaser.arrive();
            phaser.awaitAdvanceInterruptibly(2, 1000, TimeUnit.MILLISECONDS);
            List<List<Message>> messagesReceivedByEachClient = messageFutures.stream().map(m -> {
                try {
                    return m.get(500, TimeUnit.MILLISECONDS);
                } catch (ExecutionException | InterruptedException | TimeoutException e) {
                    fail("Messages were not received in time");
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());
            assertThat(messagesReceivedByEachClient).allSatisfy(messages -> {
                userNames.forEach(userName -> {
                    assertThat(messages)
                            .anyMatch(m -> m.getUserName().equals(userName) && m.getBody().equals("Hello!"));
                    assertThat(messages)
                            .anyMatch(m -> m.getUserName().equals(userName) && m.getBody().equals("My name is " + userName));
                });

            });
            //System.out.println(messagesReceivedByEachClient);
        }
        catch (InterruptedException | TimeoutException e){
            fail("Timeout reached");
            e.printStackTrace();
        }
    }

    @Test
    public void shouldStopAndRestartRoom(){
        String userName = "TestUser";
        User user = Mockito.mock(User.class);
        final ChatRoom chatRoomDomain = Mockito.mock(ChatRoom.class);
        final Phaser phaser = new Phaser(2);
        Mockito.when(chatRoomDomain.getId()).thenReturn(1L);
        Mockito.when(chatRoomDomain.getName()).thenReturn("Test chat room #1");
        Mockito.when(user.getUserName()).thenReturn(userName);
        Mockito.when(user.getId()).thenReturn(ThreadLocalRandom.current().nextLong());
        Mockito.when(userService.getUserByUserName(userName)).thenReturn(Optional.of(user));
        Mockito.when(user.getChatRooms()).thenReturn(List.of(chatRoomDomain));

        Runnable client = () -> {
            final ChatApi api =  realm.tryConnect(() -> userName);
            phaser.arriveAndAwaitAdvance();
            api.sendMessage(new Message(chatRoomDomain.getId(),"Hello!"));
            phaser.arriveAndAwaitAdvance();
            api.disconnect();
            phaser.arriveAndAwaitAdvance();
            api.sendMessage(new Message(chatRoomDomain.getId(),"Hello again! Did you restart the room?"));
            phaser.arriveAndAwaitAdvance();
        };

    }
}