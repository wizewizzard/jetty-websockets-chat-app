package com.wu.chatserver.service.chatting;

import com.wu.chatserver.exception.ChatException;
import com.wu.chatserver.service.MessageService;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChatRoomImpl implements ChatRoom{
    private static int DEFAULT_CAPACITY  = 1024;
    private static int DEFAULT_TIMEOUT = 60;
    @EqualsAndHashCode.Include
    private final com.wu.chatserver.domain.ChatRoom chatRoom;
    private final MessageService messageService;
    private final Runnable callback;
    private volatile boolean isRunning;
    BlockingQueue<Message> messages = new ArrayBlockingQueue<>(DEFAULT_CAPACITY);
    //TODO: CopyOnWriteList or synchronizedList... debatable
    List<RoomMembership> roomMembers = new CopyOnWriteArrayList<>();

    public ChatRoomImpl(com.wu.chatserver.domain.ChatRoom chatRoom,
                        MessageService messageService,
                        Runnable callback) {
        this.chatRoom = chatRoom;
        this.messageService = messageService;
        this.callback = callback;
        isRunning = false;
    }

    @Override
    public void run() {
        isRunning = true;
        log.debug("Chat room {} started.", chatRoom.getName());
        while(!Thread.interrupted()){
            try {
                Message message = messages.poll(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
                log.info("Polled message {} from queue in the chat room {}", message, chatRoom.getName());
                for (RoomMembership roomMember: roomMembers
                     ) {
                    roomMember.handleMessage(message);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        log.debug("Chat room {} is going offline", chatRoom.getName());
        isRunning = false;
        callback.run();
    }

    @Override
    public void addMembership(RoomMembership membership) {
        if(!roomMembers.contains(membership)){
            roomMembers.add(membership);
            messages.offer(new Message(chatRoom.getId(),
                    null,
                    "System",
                    "User " +  membership.getUser().getUserName() + " is online",
                    LocalDateTime.now()));
            log.info("User {} was added to the chat room {}", membership.getUser().getUserName(), chatRoom.getName());
        }
        else{
            throw new AssertionError("Wrong chat room membership management");
        }
    }

    @Override
    public void removeMembership(RoomMembership membership) {
        if(roomMembers.contains(membership)){
            roomMembers.remove(membership);
            messages.offer(new Message(chatRoom.getId(),
                    null,
                    "System",
                    "User"  +  membership.getUser().getUserName() + " goes offline",
                    LocalDateTime.now()));
            log.info("User {} was removed from the chat room {}", membership.getUser().getUserName(), chatRoom.getName());
        }
        else{
            throw new AssertionError("Wrong chat room membership management");
        }
    }

    @Override
    public void sendMessage(RoomMembership source, Message message) {
        if(roomMembers.contains(source)){
            Message m = new Message(chatRoom.getId(),
                    source.getUser().getId(),
                    source.getUser().getUserName(),
                    message.getBody(),
                    LocalDateTime.now());
            try {
                if(!messages.offer(m, 500, TimeUnit.MILLISECONDS)){
                    throw new ChatException("Unable to send message. Try again later");
                }
                //messageService.publishMessage(chatRoom.getId(), source.getUser().getId(), message.getBody());
            }
            catch (InterruptedException interruptedException){
                throw new ChatException("Unable to send message. Try again later");
            }
        }
        else {
            throw new ChatException("User is not part of the chat");
        }
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }
}
