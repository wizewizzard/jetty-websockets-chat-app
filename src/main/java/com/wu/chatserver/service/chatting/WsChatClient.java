package com.wu.chatserver.service.chatting;

public class WsChatClient implements ChatClientAPI{

    private final ChatRoomRealm chatRoomRealm;
    private final Object credentials;

    public WsChatClient(ChatRoomRealm chatRoomRealm,
                        Object credentials){
        this.chatRoomRealm = chatRoomRealm;
        this.credentials = credentials;
    }

    @Override
    public void tryConnect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void sendMessage() {

    }

    @Override
    public void pollMessage() {

    }
}
