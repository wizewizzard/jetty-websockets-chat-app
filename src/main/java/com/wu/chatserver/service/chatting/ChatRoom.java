package com.wu.chatserver.service.chatting;

import java.util.concurrent.TimeoutException;

    public void addMembership(RoomConnection membership);

    public void sendMessage(RoomMembership source, Message message) throws TimeoutException, InterruptedException;

    public boolean isRunning();

    public void setUpTime(int uptimeInSeconds);
}
