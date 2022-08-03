package com.wu.chatserver.service.chatting;

import com.wu.chatserver.exception.ChatException;

import java.util.Objects;

public class ChatClientAPI {

    private final ChatRoomRealm chatRoomRealm;

    private ChatApi chatApi;

    public ChatClientAPI(ChatRoomRealm chatRoomRealm){
        this.chatRoomRealm = chatRoomRealm;
    }

    public void disconnect() {
        Objects.requireNonNull(chatApi);
        chatApi.disconnect();
        chatApi = null;
    }

    public void connect(ConnectionCredentials connectionCredentials) {
        chatApi = chatRoomRealm.tryConnect(connectionCredentials);
    }

    public void sendMessage(Message message) {
        Objects.requireNonNull(chatApi);
        chatApi.sendMessage(message);
    }

    public Message pollMessage() throws ChatException, InterruptedException {
        Objects.requireNonNull(chatApi);
        return  chatApi.pollMessage();
    }
}
