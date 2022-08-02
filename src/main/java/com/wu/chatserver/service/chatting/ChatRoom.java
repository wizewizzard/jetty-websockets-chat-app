package com.wu.chatserver.service.chatting;

public interface ChatRoom extends Runnable{

    public void addMembership(RoomMembership membership);

    public void removeMembership(RoomMembership membership);

    public void sendMessage(RoomMembership source, Message message);
    public boolean isRunning();

}
