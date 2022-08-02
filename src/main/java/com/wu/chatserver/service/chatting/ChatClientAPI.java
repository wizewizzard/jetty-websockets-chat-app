package com.wu.chatserver.service.chatting;

public interface ChatClientAPI {
    public void tryConnect();
    public void disconnect();
    public void sendMessage();
    void pollMessage();
}
