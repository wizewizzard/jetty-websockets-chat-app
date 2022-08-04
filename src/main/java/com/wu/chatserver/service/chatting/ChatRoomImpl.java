package com.wu.chatserver.service.chatting;

import com.wu.chatserver.exception.ChatException;
import com.wu.chatserver.service.MessageService;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChatRoomImpl implements ChatRoom{
    private static int DEFAULT_CAPACITY  = 1024;
    private static int DEFAULT_UPTIME = 60;
    @EqualsAndHashCode.Include
    private final com.wu.chatserver.domain.ChatRoom chatRoom;
    private final MessageService messageService;
    private final Runnable callback;
    @Getter
    @Setter
    private int upTime;
    private volatile boolean isRunning;
    BlockingQueue<Message> messages = new ArrayBlockingQueue<>(DEFAULT_CAPACITY);
    List<RoomMembership> roomMembers = Collections.synchronizedList(new ArrayList<>());

    public ChatRoomImpl(com.wu.chatserver.domain.ChatRoom chatRoom,
                        MessageService messageService,
                        Runnable callback) {
        this.chatRoom = chatRoom;
        this.messageService = messageService;
        this.callback = callback;
        isRunning = false;
        upTime = DEFAULT_UPTIME;
    }

    @Override
    public void run() {
        isRunning = true;
        log.debug("Chat room {} started.", chatRoom.getName());
        try {
            while(!Thread.interrupted()){
                Message message = messages.poll(upTime, TimeUnit.SECONDS);
                log.info("Polled message {} from queue in the chat room {} sending to {} number of users",
                        message, chatRoom.getName(), roomMembers.size());
                if(message == null){
                    break;
                }

                for (RoomMembership roomMember: roomMembers
                     ) {
                    roomMember.handleMessage(message);
                }
            }
        }
        catch (InterruptedException e) {
        }
        log.debug("Chat room {} is going offline", chatRoom.getName());
        isRunning = false;
        if(callback != null)
            callback.run();
    }

    @Override
    public void addMembership(RoomMembership membership) {
        if(!roomMembers.contains(membership)){
            roomMembers.add(membership);
            membership.addChatRoom(this);
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
            membership.removeChatRoom(this);
            messages.offer(new Message(chatRoom.getId(),
                    null,
                    "System",
                    "User "  +  membership.getUser().getUserName() + " goes offline",
                    LocalDateTime.now()));
            log.info("User {} was removed from the chat room {}", membership.getUser().getUserName(), chatRoom.getName());
        }
        else{
            throw new AssertionError("Wrong chat room membership management");
        }
    }

    @Override
    public void sendMessage(RoomMembership source, Message message) throws InterruptedException, TimeoutException {
        if(roomMembers.contains(source)){
            Message m = new Message(chatRoom.getId(),
                    source.getUser().getId(),
                    source.getUser().getUserName(),
                    message.getBody(),
                    LocalDateTime.now());
                if(!messages.offer(m, 500, TimeUnit.MILLISECONDS)){
                    throw new TimeoutException("Unable to send message. Try again later");
                }
                //messageService.publishMessage(chatRoom.getId(), source.getUser().getId(), message.getBody());
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
