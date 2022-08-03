package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This interface supposed to know how to contact with specific user.
 */
//TODO: think about naming of this class
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoomConnection {
    @EqualsAndHashCode.Include
    @Getter
    private final User user;
    private final Consumer<Message> messageHandler;
    @Getter
    private final Set<ChatRoom> chatRooms;

    public RoomConnection(User user,
                          Consumer<Message> messageHandler) {
        this.user = user;
        this.messageHandler = messageHandler;
        this.chatRooms = Collections.synchronizedSet(new HashSet<>());
    }

    public void handleMessage(Message message) {
        this.messageHandler.accept(message);
    }

    public void addChatRoom(ChatRoom chatRoom) {
        chatRoom.addMembership(this);
        chatRooms.add(chatRoom);
    }

    public void removeChatRoom(ChatRoom chatRoom) {
        chatRoom.removeMembership(this);
        chatRooms.remove(chatRoom);
    }

    public void closeConnection() {
        chatRooms.forEach(cr -> cr.removeMembership(this));
        chatRooms.clear();
    }

}
