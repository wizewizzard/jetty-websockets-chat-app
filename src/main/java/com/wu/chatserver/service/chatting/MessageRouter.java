package com.wu.chatserver.service.chatting;

import com.wu.chatserver.exception.ChatException;

/**
 * The instance of this class provided for every connection.
 * Websocket interacts with chat rooms using the instance of this class.
 */
public interface MessageRouter {
    /**
     * Sends message
     * @param message
     * @throws ChatException
     */
    public void sendMessage(Long chatRoomId, String message) throws ChatException;
}
