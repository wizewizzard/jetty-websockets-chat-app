package com.wu.chatserver.service.chatting;

import com.wu.chatserver.exception.ChatException;

import java.util.Optional;
import java.util.function.Supplier;

public interface ChatRoomRealm {
        /**
         * Makes user go online in all chat rooms he is member of
         * @param credentials
         * @return
         * @throws ChatException
         */
        public ChatApi tryConnect(ConnectionCredentials credentials) throws ChatException;
        /**
         * Makes user go offline in all chat rooms he is member of
         * @param credentials
         * @return
         * @throws ChatException
         */
        public void disconnect(String credentials);

/*        *//**
         * Get chat room instance by id
         * @param chatRoomId
         * @return
         *//*
        public Optional<ChatRoom> getChatRoom(Long chatRoomId);*/

}
