package com.wu.chatserver.service.chatting;

import com.wu.chatserver.exception.ChatException;

import java.util.function.Consumer;

public class WsChatClient implements ChatClientAPI{
    private final MessagePoller<Message> pollMessage;
    private final Runnable onDisconnect;
    private final Consumer<Message> messageSender;

    public WsChatClient(
                        Consumer<Message> messageSender,
                        MessagePoller<Message> pollMessage,
                        Runnable onDisconnect
                        ){
        this.messageSender = messageSender;
        this.pollMessage = pollMessage;
        this.onDisconnect = onDisconnect;
    }


    @Override
    public void disconnect() {
        onDisconnect.run();
    }

    @Override
    public void sendMessage(Message message) {
        messageSender.accept(message);
    }

    @Override
    public Message pollMessage() throws ChatException, InterruptedException {
        return  pollMessage.get();
    }
}
