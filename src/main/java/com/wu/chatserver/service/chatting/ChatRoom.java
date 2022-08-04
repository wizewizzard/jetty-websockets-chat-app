package com.wu.chatserver.service.chatting;

import java.util.concurrent.TimeoutException;

public interface ChatRoom extends Runnable {

    public void addMembership(RoomMembership membership);

    public void removeMembership(RoomMembership membership);

    public void sendMessage(RoomMembership source, Message message) throws TimeoutException, InterruptedException;

    public boolean isRunning();

    public void setUpTime(int uptimeInSeconds);
}
