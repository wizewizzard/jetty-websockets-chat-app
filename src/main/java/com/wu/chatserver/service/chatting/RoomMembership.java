package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 *
 */
@Slf4j
public class RoomMembership {
    @Getter
    private final User user;
    @Setter
    @Getter
    private MessageConsumer<Message> messageConsumer;

    @Getter
    private final Set<ChatRoom> chatRooms;

    public RoomMembership(User user) {
        this.user = user;
        this.chatRooms = Collections.synchronizedSet(new HashSet<>());
    }

    public RoomMembership(User user, MessageConsumer<Message> messageConsumer) {
        this.user = user;
        this.messageConsumer = messageConsumer;
        this.chatRooms = Collections.synchronizedSet(new HashSet<>());
    }

    public void handleMessage(Message message) {
        if(messageConsumer != null){
            try{
                messageConsumer.accept(message);
            }
            catch (InterruptedException | TimeoutException exception){
                log.error("Message was not handled");
            }
        }
        else{
            log.error("Message consumer is not set");
        }
    }

    public void addChatRoom(ChatRoom chatRoom) {
        chatRooms.add(chatRoom);
    }

    public void removeChatRoom(ChatRoom chatRoom) {
        chatRooms.remove(chatRoom);
    }
}
