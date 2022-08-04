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

@Slf4j
@ApplicationScoped
public class WsChatRoomRealm implements ChatRoomRealm {
    private final ConnectionPool connectionPool;
    private final ReadWriteLock chatRoomLock = new ReentrantReadWriteLock();
    private final Map<Long, ChatRoom> chatRooms = new HashMap<>();
    private final ExecutorService roomWorkers = Executors.newFixedThreadPool(8);
    private final UserService userService;
    private final ChatRoomService chatRoomService;
    private final MessageService messageService;
    private int roomUpTime;

    @Inject
    public WsChatRoomRealm(ConnectionPool connectionPool,
                           UserService userService,
                           ChatRoomService chatRoomService,
                           MessageService messageService) {
        this.connectionPool = connectionPool;
        this.userService = userService;
        this.messageService = messageService;
        this.chatRoomService = chatRoomService;
    }

    @Override
    public void init(Properties properties) {
        System.out.println("Init called");
        ChatRoomRealm.super.init(properties);
        String roomUpTime = properties.getProperty("RoomUpTime");
        if (roomUpTime != null)
            this.roomUpTime = Integer.parseInt(roomUpTime);
        else
            this.roomUpTime = 0;
        log.info("Rooms default uptime is set to {}", this.roomUpTime);
    }

    @Override
    public ChatApi tryConnect(ConnectionCredentials credentials) throws ChatException {
        log.info("Trying to connect a user");
        final BlockingQueue<Message> userMessageQueue = new ArrayBlockingQueue<>(32);
        RoomConnection roomConnection = connectionPool.establishConnection(credentials);
        RoomMembership roomMembership = new RoomMembership(roomConnection.getUser(), userMessageQueue::offer);
        roomConnection.setMembership(roomMembership);

        List<com.wu.chatserver.domain.ChatRoom> chatRoomsDomain =
                chatRoomService.findChatRoomsForUser(roomConnection.getUser().getUserName());

        for (com.wu.chatserver.domain.ChatRoom chatRoomDomain : chatRoomsDomain) {
            ChatRoom chatRoom;
            chatRoomLock.readLock().lock();
            try {
                chatRoom = chatRooms.get(chatRoomDomain.getId());
            } finally {
                chatRoomLock.readLock().unlock();
            }
            if(chatRoom == null){
                chatRoom = initChatRoom(chatRoomDomain);
                synchronizeMembership(chatRoom, chatRoomDomain);
            }
            if(!chatRoom.isRunning()){
                launchChatRoom(chatRoom);
            }
            chatRoom.addMembership(roomMembership);
        }

        log.info("User was connected");
        return new ChatApi(
                (Message m) -> {
                    log.debug("Processing ws message {}", m);
                    Objects.requireNonNull(m);
                    if (m.getChatId() == null || m.getBody() == null)
                        throw new ChatException("Invalid message format");
                    dispatchMessage(roomMembership, m);
                },
                () -> {
                    log.debug("Waiting a message from the queue");
                    return userMessageQueue.take();
                },
                () -> {
                    connectionPool.closeConnection(roomConnection);
                });
    }

    private void dispatchMessage(RoomMembership membership, Message message) throws InterruptedException, TimeoutException {
        ChatRoom chatRoom;

        chatRoomLock.readLock().lock();
        try {
            chatRoom = chatRooms.get(message.getChatId());
        } finally {
            chatRoomLock.readLock().unlock();
        }
        if (chatRoom == null) {
            throw new ChatException("Specified chat room does not exist");
        }
        if (!chatRoom.isRunning()) {
            launchChatRoom(chatRoom);
        }
        chatRoom.sendMessage(membership, message);
    }

    /**
     * Puts a new chat room in a map but does not run it
     * puts available memberships in it
     */
    private ChatRoom initChatRoom(com.wu.chatserver.domain.ChatRoom chatRoomDomain) {
        /*com.wu.chatserver.domain.ChatRoom chatRoomDomain = chatRoomService
                .findChatRoomWithMembersById(chatRoomId)
                .orElseThrow(() -> new ChatException("Given chat room doe not exist"));*/
        chatRoomLock.writeLock().lock();
        try {
            log.debug("Initialising room {} with uptime of {} seconds", chatRoomDomain.getName(), roomUpTime);
            ChatRoom chatRoom = new ChatRoomImpl(chatRoomDomain,
                    messageService,
                    null
            );
            if (roomUpTime > 0) {
                chatRoom.setUpTime(roomUpTime);
            }

            /*Set<User> chatMembers = chatRoomDomain.getMembers();
            for (User chatMember : chatMembers) {
                List<RoomConnection> userConnections = connectionPool.getUserConnections(chatMember);
                if (userConnections != null)
                    userConnections.forEach(conn -> {
                        chatRoom.addMembership(conn.getMembership());
                        userService.setUserOnlineStatusForRoom(chatRoomDomain.getId(), chatMember.getId(), UsersChatSession.OnlineStatus.ONLINE);
                    });
            }*/

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

    public void synchronizeMembership(ChatRoom chatRoom, com.wu.chatserver.domain.ChatRoom chatRoomDomain){
        Set<User> chatMembers = chatRoomDomain.getMembers();
        for (User chatMember : chatMembers) {
            List<RoomConnection> userConnections = connectionPool.getUserConnections(chatMember);
            if (userConnections != null)
                userConnections.forEach(conn -> {
                    chatRoom.addMembership(conn.getMembership());
                    //userService.setUserOnlineStatusForRoom(chatRoomDomain.getId(), chatMember.getId(), UsersChatSession.OnlineStatus.ONLINE);
                });
        }
    }

    /**
     * Launches a thread for the given chatroom
     *
     * @param chatRoom initialized chat room
     * @return
     */
    private ChatRoom launchChatRoom(ChatRoom chatRoom) {
        Objects.requireNonNull(chatRoom);
        log.debug("Launching room {}", chatRoom);
        chatRoomLock.writeLock().lock();
        try {
            if (!chatRoom.isRunning()) {
                roomWorkers.submit(chatRoom);
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
        log.trace("Triggered chatRoomCreated");
        //trying to find all members of the room in opened sessions and put them in it
        com.wu.chatserver.domain.ChatRoom chatRoomDomain = event.getChatRoom();
        User user = event.getCreatedBy();
        if (chatRooms.get(chatRoomDomain.getId()) == null) {
            ChatRoom chatRoom = initChatRoom(chatRoomDomain);
            synchronizeMembership(chatRoom, chatRoomDomain);
        }
        launchChatRoom(chatRooms.get(chatRoomDomain.getId()));
        List<RoomConnection> userConnections = connectionPool.getUserConnections(user);
        //It is not enough to acquire online status only by becoming a member of a chat room.
        // User must have connections opened
        if (userConnections != null) {
            userService.setUserOnlineStatusForRoom(chatRoomDomain.getId(), user.getId(), UsersChatSession.OnlineStatus.ONLINE);
        }
    }

    /**
     * If ws connection is opened adds user connection to the room.
     * If user did it without connection opened, does nothing
     *
     * @param event
     */
    public void userJoinedChatRoom(@Observes UserJoinedChatRoom event) {
        log.trace("Triggered userJoinedChat");
        com.wu.chatserver.domain.ChatRoom chatRoomDomain = event.getChatRoom();
        User user = event.getUser();
        List<RoomConnection> userConnections = connectionPool.getUserConnections(user);
        //init, synch membership and launch room
        if (chatRooms.get(chatRoomDomain.getId()) == null) {
            ChatRoom chatRoom = initChatRoom(chatRoomDomain);
            synchronizeMembership(chatRoom, chatRoomDomain);
            launchChatRoom(chatRoom);
        }
        //if room is initialised but down launch it and add membership for the room
        else {
            if(!chatRooms.get(chatRoomDomain.getId()).isRunning()){
                final ChatRoom chatRoom = chatRooms.get(chatRoomDomain.getId());
                launchChatRoom(chatRoom);
            }
            final ChatRoom chatRoom = chatRooms.get(chatRoomDomain.getId());
            if (userConnections != null) {
                userConnections.forEach(c -> chatRoom.addMembership(c.getMembership()));
            }
        }
        if (userConnections != null) {
            userService.setUserOnlineStatusForRoom(chatRoomDomain.getId(), user.getId(), UsersChatSession.OnlineStatus.ONLINE);
        }
    }

    /**
     * If ws connection is opened removes connection from room.
     * If user did it without connection opened, does nothing
     *
     * @param event
     */
    public void userLeftChatRoom(@Observes UserLeftChatRoom event) {
        log.trace("Triggered userLeftChat");
        //find user connection
        //if it exists then remove user membership from it
        com.wu.chatserver.domain.ChatRoom chatRoomDomain = event.getChatRoom();
        User user = event.getUser();
        ChatRoom chatRoom = chatRooms.get(chatRoomDomain.getId());
        List<RoomConnection> userConnections = connectionPool.getUserConnections(user);
        if (chatRoom != null && userConnections != null) {
            userConnections.forEach(c -> chatRoom.removeMembership(c.getMembership()));
            userService.setUserOnlineStatusForRoom(chatRoomDomain.getId(), user.getId(), UsersChatSession.OnlineStatus.OFFLINE);
        }

    }
}
