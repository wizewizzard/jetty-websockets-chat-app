package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import com.wu.chatserver.domain.UsersChatSession;
import com.wu.chatserver.exception.ChatException;
import com.wu.chatserver.service.ChatRoomService;
import com.wu.chatserver.service.MessageService;
import com.wu.chatserver.service.UserService;
import com.wu.chatserver.service.chatting.event.ChatRoomCreated;
import com.wu.chatserver.service.chatting.event.UserJoinedChatRoom;
import com.wu.chatserver.service.chatting.event.UserLeftChatRoom;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@ApplicationScoped
public class WsChatRoomRealm implements ChatRoomRealm {
    private final Map<User, List<RoomConnection>> userOpenedConnections = new ConcurrentHashMap<>();
    private final ReadWriteLock chatRoomLock = new ReentrantReadWriteLock();
    private final Map<Long, ChatRoom> chatRooms = new HashMap<>();
    private final ExecutorService roomWorkers = Executors.newFixedThreadPool(8);
    private final UserService userService;
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private int roomUpTime;

    @Inject
    public WsChatRoomRealm(UserService userService,
                           ChatRoomService chatRoomService,
                           MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
        this.chatRoomService = chatRoomService;
        log.info("Rooms default uptime is set to {}", this.roomUpTime);
    }

    @Override
    public void init(Properties properties) {
        System.out.println("Init called");
        ChatRoomRealm.super.init(properties);
        String roomUpTime = properties.getProperty("RoomUpTime");
        if(roomUpTime != null)
            this.roomUpTime = Integer.parseInt(roomUpTime);
        else
            this.roomUpTime = 0;
    }

    @Override
    public ChatApi tryConnect(ConnectionCredentials credentials) throws ChatException {
        //TODO: method looks ugly. refactor later
        log.info("Trying to connect a user");

        User user = userService.getUserWithChatRooms(credentials.getUserName())
                .orElseThrow(() -> new ChatException("No user with given credentials found"));
        List<com.wu.chatserver.domain.ChatRoom> userChatRoomsDomain = user.getChatRooms();
        BlockingQueue<Message> queue = new ArrayBlockingQueue<>(32);
        final RoomConnection roomConnection = new RoomConnection(user, queue::offer);

        for (com.wu.chatserver.domain.ChatRoom chatRoomDomain : userChatRoomsDomain) {
            addMemberForRoom(roomConnection, chatRoomDomain.getId());
        }

        userOpenedConnections.merge(user,
                Collections.synchronizedList(new LinkedList<>(List.of(roomConnection))),
                (l1, l2) -> Collections.synchronizedList(Stream.concat(l1.stream(), l2.stream()).collect(Collectors.toList())));

        log.info("User was connected");
        return new ChatApi(
                (Message m) -> {
                    log.debug("Processing ws message {}", m);
                    if(m.getChatId() == null || m.getBody() == null)
                        throw new ChatException("Invalid message format");
                    dispatchMessage(roomConnection, m);
                },
                () -> {
                    log.debug("Waiting a message from the queue");
                    return queue.take();
                },
                () -> {
                    log.debug("Disconnecting user {}", user.getUserName());

                    List<RoomConnection> roomConnections = userOpenedConnections.get(user);
                    roomConnection.closeConnection();
                    roomConnections.remove(roomConnection);
                    if(roomConnections.isEmpty()){
                        userService.userSetOnlineStatus(user.getId(), UsersChatSession.OnlineStatus.OFFLINE);
                    }
                    log.debug("Connection for user {} was removed", user.getUserName());
                });
    }

    private void dispatchMessage(RoomConnection connection, Message message) {
        ChatRoom chatRoom;
        chatRoomLock.readLock().lock();
        try {
            chatRoom = chatRooms.get(message.getChatId());
        } finally {
            chatRoomLock.readLock().unlock();
        }
        if (chatRoom == null)
            throw new ChatException("This chat room does not exist");
        if (!chatRoom.isRunning()) {
            launchChatRoom(chatRoom);
        }
        chatRoom.sendMessage(connection, message);
    }

    /**
     * Puts a new chat room in a map but does not run it
     * puts available memberships in it
     */
    private ChatRoom initChatRoom(Long chatRoomId) {
        com.wu.chatserver.domain.ChatRoom chatRoomDomain = chatRoomService
                .findChatRoomWithMembersById(chatRoomId)
                .orElseThrow();
        chatRoomLock.writeLock().lock();
        try {
            log.debug("Initialising room {}", chatRoomDomain.getName());
            ChatRoom chatRoom;
            if(roomUpTime > 0){
                chatRoom = new ChatRoomImpl(chatRoomDomain,
                        messageService,
                        roomUpTime,
                        null
                );
            }
            else{
                chatRoom = new ChatRoomImpl(chatRoomDomain,
                        messageService,
                        null
                );
            }

            Set<User> chatMembers = chatRoomDomain.getMembers();
            userOpenedConnections.keySet()
                    .forEach(user -> {
                        if (chatMembers.contains(user)) {
                            for (RoomConnection connection : userOpenedConnections.get(user)) {
                                connection.addChatRoom(chatRoom);
                                chatRoom.addMembership(connection);
                            }
                        }
                    });
            //CAS
            if (chatRooms.get(chatRoomDomain.getId()) == null) {
                chatRooms.put(chatRoomDomain.getId(), chatRoom);
                log.debug("Room {} initialized", chatRoomDomain.getName());
            }
            return chatRooms.get(chatRoomDomain.getId());
        } finally {
            chatRoomLock.writeLock().unlock();
        }
    }

    /**
     * Launches a thread for the given chatroom
     *
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
        } finally {
            chatRoomLock.writeLock().unlock();
        }
    }

    @Override
    public void disconnect(String userName) {
        //update sessions if this is the last user's connection!
        //remove session from map and remove member from rooms
        throw new UnsupportedOperationException("Not implemented");
    }

    public void chatRoomCreated(@Observes ChatRoomCreated event) {
        log.info("Triggered chatRoomCreated");
        //trying to find all members of the room in opened sessions and put them in it

        }
    }

    private void addMemberForRoom(RoomConnection roomConnection, Long chatRoomId){
        chatRoomLock.readLock().lock();
        ChatRoom chatRoom = chatRooms.get(chatRoomId);
        chatRoomLock.readLock().unlock();
        if (chatRoom == null) {
            chatRoom = initChatRoom(chatRoomId);
        }
        roomConnection.addChatRoom(chatRoom);
        if(userOpenedConnections.get(roomConnection.getUser()) == null){
            userService.userSetOnlineStatus(roomConnection.getUser().getId(), UsersChatSession.OnlineStatus.ONLINE);
        }
    }

    /**
     * If ws connection is opened adds user connection to the room.
     * If user did it without connection opened, does nothing
     *
     * @param event
     */
    public void userJoinedChat(@Observes UserJoinedChatRoom event) {
        log.info("Triggered userJoinedChat");
        //find user connection
        //if it exists then get the chat room and put user there
    }

    /**
     * If ws connection is opened removes connection from room.
     * If user did it without connection opened, does nothing
     *
     * @param event
     */
    public void userLeftChat(@Observes UserLeftChatRoom event) {
        log.info("Triggered userLeftChat");
        //find user connection
        //if it exists then remove user membership from it
    }
}
