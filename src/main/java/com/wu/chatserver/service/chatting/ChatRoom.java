package com.wu.chatserver.service.chatting;

public interface ChatRoom extends Runnable{

    public void addMembership(RoomConnection membership);

    public void removeMembership(RoomConnection membership);

    public void sendMessage(RoomConnection source, Message message);
    public boolean isRunning();

    public void setUpTime(int uptimeInSeconds);
}
