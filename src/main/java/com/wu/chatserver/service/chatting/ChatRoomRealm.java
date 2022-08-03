package com.wu.chatserver.service.chatting;

import com.wu.chatserver.exception.ChatException;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

public interface ChatRoomRealm {
        public default void init(Properties properties){

        }
        /**
         * Makes user go online in all chat rooms he is member of
         * @param credentials
         * @return
         * @throws ChatException
         */
        public ChatApi tryConnect(ConnectionCredentials credentials) throws ChatException;
        /**
         * Forces user to be disconnected
         * @param credentials
         * @return
         * @throws ChatException
         */
        public void disconnect(String credentials);

}
