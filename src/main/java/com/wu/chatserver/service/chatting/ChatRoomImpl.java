package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import com.wu.chatserver.dto.MessageDTO;
import com.wu.chatserver.exception.ChatException;
import com.wu.chatserver.service.MessageService;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ChatRoomImpl implements ChatRoom, Runnable{
    private com.wu.chatserver.domain.ChatRoom chatRoom;
    private static int DEFAULT_CAPACITY  = 1024;

    private MessageService messageService;
    BlockingQueue<MessageDTO.Response.MessageWithAuthor> messages = new ArrayBlockingQueue<>(DEFAULT_CAPACITY);
    //TODO: CopyOnWriteList or synchronizedList... debatable
    List<UserSessions> roomMembers = new CopyOnWriteArrayList<>();

    @Override
    public void init(com.wu.chatserver.domain.ChatRoom chatRoom) {
       this.chatRoom = chatRoom;
    }

    @Override
    public void addMember(User user) {
        /*messages.offer("User ... connected", 1000, TimeUnit.MILLISECONDS);
        roomMembers.add();*/
        roomMembers.add(user);
        messages.offer(new MessageDTO.Response.MessageWithAuthor("User is online", LocalDateTime.now(), "System"));

    }

    @Override
    public void removeUser(User user) {
        //move user to offline
        //user disconnected from chat ....
        roomMembers.remove(user);
        if(! roomMembers.contains(user)){
            //close session
        }
        messages.offer(new MessageDTO.Response.MessageWithAuthor("User goes offline", LocalDateTime.now(), "System"));
    }

    @Override
    public void sendMessage(User user, String message) {
        //put message in queue and save it the to db
        //messages.put("User ... connected");
        messageService.publishMessage(chatRoom.getId(), user.getId(), message);
        messages.offer(new MessageDTO.Response.MessageWithAuthor(message,  LocalDateTime.now(), user.getUserName()));

    }

    @Override
    public void run() {
        log.debug("Chat room {} started.", chatRoom.getName());
        while(!Thread.interrupted()){
            try {
                MessageDTO.Response.MessageWithAuthor message = messages.take();
                for(UserSessions userSessions )
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
