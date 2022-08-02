package com.wu.chatserver.service.chatting;

import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChatConnection {
    @Getter
    private final RoomMembership membership;
    private final Set<ChatRoom> chatRooms = Collections.synchronizedSet(new HashSet<>());

    public ChatConnection(RoomMembership membership) {
        this.membership = membership;
    }

    public void addChatRoom(ChatRoom chatRoom){
        chatRooms.add(chatRoom);
    }

    public void removeChatRoom(ChatRoom chatRoom){
        chatRooms.remove(chatRoom);
    }

    public void closeConnection(){
        for (ChatRoom chatRoom: chatRooms)
            chatRoom.removeMembership(membership);
    }
}
