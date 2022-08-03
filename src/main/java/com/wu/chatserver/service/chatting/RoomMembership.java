package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.function.Consumer;

/**
 * This interface supposed to know how to contact with specific user.
 */
//TODO: think about naming of this class
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoomMembership {
        @EqualsAndHashCode.Include
        @Getter
        private final User user;
        private final Consumer<Message> messageHandler;

        public RoomMembership(User user,
                              Consumer<Message> messageHandler) {
            this.user = user;
            this.messageHandler = messageHandler;
        }
        public void handleMessage(Message message){
            this.messageHandler.accept(message);
        }

}
