package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import com.wu.chatserver.exception.ChatException;

import java.util.Optional;

public class MessageRouterImpl implements MessageRouter{
    private User user;
    private ChatRoomRealm realm;

    public MessageRouterImpl(User user, ChatRoomRealm realm) {
        this.user = user;
        this.realm = realm;
    }

    @Override
    public void sendMessage(Long chatRoomId, String message) throws ChatException {
        //
        Optional chatRoom = realm.getChatRoom(chatRoomId);
        chatRoom.sendMessage(user, message);
    }
}
