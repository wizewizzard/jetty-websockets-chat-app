package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import com.wu.chatserver.exception.ChatException;

public interface ChatRoom {

    /**
     * Initialize a chat room from domain object
     */
    void init(com.wu.chatserver.domain.ChatRoom chatRoom);

    public void addMember(User credentials);

    public void removeUser(User credentials);

    public void sendMessage(User credentials, String message);

}
