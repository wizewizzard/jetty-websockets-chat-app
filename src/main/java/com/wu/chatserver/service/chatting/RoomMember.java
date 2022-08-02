package com.wu.chatserver.service.chatting;

import com.wu.chatserver.dto.MessageDTO;

/**
 * This interface supposed to know how to contact with specific user.
 */
public interface RoomMember {

    /**
     * Initiate a connection close for a chat member
     */
    void closeConnection();

    /**
     * Send message to room member
     * @param message
     */
    void handleMessage(MessageDTO.Response.MessageWithAuthor message);
}
