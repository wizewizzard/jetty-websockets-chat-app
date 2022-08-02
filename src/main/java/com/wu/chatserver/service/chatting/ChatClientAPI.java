package com.wu.chatserver.service.chatting;

import com.wu.chatserver.exception.ChatException;

public interface ChatClientAPI {
    public void disconnect();
    public void  sendMessage(Message message) throws ChatException;
    public Message pollMessage() throws ChatException;
}
