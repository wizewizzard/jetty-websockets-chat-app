package com.wu.chatserver.service.chatting.event;

import com.wu.chatserver.domain.User;
import com.wu.chatserver.service.chatting.Message;
import lombok.Data;

@Data
public class MessageReceived {
    private User user;
    private Message message;

    public MessageReceived(User user, Message message) {
        this.user = user;
        this.message = message;
    }
}
