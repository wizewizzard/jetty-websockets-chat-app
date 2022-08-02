package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import com.wu.chatserver.exception.ChatException;
import com.wu.chatserver.service.ChatRoomService;
import com.wu.chatserver.service.MessageService;
import com.wu.chatserver.service.UserService;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Slf4j
public class WsChatRoomRealm implements ChatRoomRealm{
    //private final Map<User, List<RoomMembership>> userOpenedSessions = new ConcurrentHashMap<>();
    private final Map<User, List<ChatConnection>> userOpenedConnections = new ConcurrentHashMap<>();
    private final ReadWriteLock chatRoomLock = new ReentrantReadWriteLock();
    private final Map<Long, ChatRoom> chatRooms = new HashMap<>();
    private final ExecutorService roomWorkers = Executors.newFixedThreadPool(8);
    private final UserService userService;
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;

    @Inject
    public WsChatRoomRealm(UserService userService,
                           ChatRoomService chatRoomService,
                           MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
        this.chatRoomService = chatRoomService;
    }

    @Override
    public ChatClientAPI tryConnect(ConnectionCredentials credentials) throws ChatException {
        //TODO: update chat sessions!
        //TODO: optimize query so it extracts user and chat rooms at once
        log.info("Trying to connect a user");

        User user = userService.getUserByUserName(credentials.getUserName())
                .orElseThrow(() -> new ChatException("No user with given credentials found"));
        List<com.wu.chatserver.domain.ChatRoom> userChatRoomsDomain = user.getChatRooms();
        BlockingQueue<Message> queue = new ArrayBlockingQueue<>(32);
        final RoomMembership membership = new RoomMembership(user, (m) -> queue.offer(m));
        ChatConnection chatConnection = new ChatConnection(membership);

        for (com.wu.chatserver.domain.ChatRoom chatRoomDomain : userChatRoomsDomain){
            chatRoomLock.readLock().lock();
            ChatRoom chatRoom = chatRooms.get(chatRoomDomain.getId());
            chatRoomLock.readLock().unlock();
            if(chatRoom == null){
                chatRoom = initChatRoom(chatRoomDomain);
            }

            chatRoom.addMembership(membership);
            chatConnection.addChatRoom(chatRoom);
        }
        userOpenedConnections.merge(user,
                Collections.synchronizedList(new LinkedList<>(List.of(chatConnection))),
                (l1, l2) -> Collections.synchronizedList(Stream.concat(l1.stream(), l2.stream()).collect(Collectors.toList())));

        log.info("User was connected");
        return new WsChatClient(
                (Message m) -> {
                    log.info("Processing ws message {}", m);
                    Objects.requireNonNull(m.getChatId());
                    dispatchMessage(membership, m);
                },
                () -> {
                    try {
                        return queue.take();
                    } catch (InterruptedException e) {
                        throw new ChatException("Interrupted");
                    }
                },
                () -> {
                    log.info("Disconnecting user {}", user.getUserName());
                    chatConnection.closeConnection();
                    userOpenedConnections.remove(chatConnection);
                    log.info("Connection for user {} was removed", user.getUserName());
                });
    }

    private void dispatchMessage(RoomMembership membership, Message message){
        ChatRoom chatRoom;
        chatRoomLock.readLock().lock();
        try{
            chatRoom = chatRooms.get(message.getChatId());
        }
        finally {
            chatRoomLock.readLock().unlock();
        }
        if(chatRoom == null )
            throw new ChatException("This chat room does not exist");
        if(!chatRoom.isRunning()){
            launchChatRoom(chatRoom);
        }
        chatRoom.sendMessage(membership, message);
    }

    /**
     * Puts a new chat room in a map but does not run it
     * puts available memberships in it
     */
    private ChatRoom initChatRoom(com.wu.chatserver.domain.ChatRoom chatRoomDomain){
        chatRoomLock.writeLock().lock();
        try {
            log.debug("Initialising room {}", chatRoomDomain.getName());
            ChatRoom chatRoom = new ChatRoomImpl(chatRoomDomain,
                    messageService,
                    () -> {
                        chatRoomLock.writeLock().lock();
                        try{
                            chatRooms.remove(chatRoomDomain.getId());
                        }
                        finally {
                            chatRoomLock.writeLock().unlock();
                        }
                    }
            );
            //TODO: optimise so it has members already fetched
            Set<User> chatMembers = chatRoomDomain.getMembers();
            userOpenedConnections.keySet()
                    .forEach(user -> {
                        if (chatMembers.contains(user)) {
                            for (ChatConnection connection : userOpenedConnections.get(user)){
                                connection.addChatRoom(chatRoom);
                                chatRoom.addMembership(connection.getMembership());
                            }
                        }
                    });
            //CAS
            if (chatRooms.get(chatRoomDomain.getId()) == null) {
                chatRooms.put(chatRoomDomain.getId(), chatRoom);
                log.debug("Room {} initialized", chatRoomDomain.getName());
            }
            return chatRooms.get(chatRoomDomain.getId());
        }
        finally {
            chatRoomLock.writeLock().unlock();
        }
    }

    /**
     * Launches a thread for the given chatroom
     * @param
     * @return
     */
    private ChatRoom launchChatRoom(ChatRoom chatRoom) {
        Objects.requireNonNull(chatRoom);
        log.debug("Launching room {}", chatRoom);
        chatRoomLock.writeLock().lock();
        try {
            if (!chatRoom.isRunning()) {
                roomWorkers.submit(chatRoom);
                log.debug("Room {} thread started", chatRoom);
            }
            return chatRoom;
            }
        finally {
            chatRoomLock.writeLock().unlock();
        }
    }

    @Override
    public void disconnect(String userName) {
        //update sessions if this is the last user's connection!
        //remove session from map and remove member from rooms
    }

    public void chatRoomCreated(@Observes Object event){

    }


    /**
     * If ws connection is opened adds user connection to the room.
     * If user did it without connection opened, does nothing
     * @param event
     */
    public void userBecameMemberOfChat(@Observes Object event){

    }

    /**
     * If ws connection is opened removes connection from room.
     * If user did it without connection opened, does nothing
     * @param event
     */
    public void userLeftChat(@Observes Object event){

    }
}
