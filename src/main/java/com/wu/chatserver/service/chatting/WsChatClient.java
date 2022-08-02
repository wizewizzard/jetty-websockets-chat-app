package com.wu.chatserver.service.chatting;

import com.wu.chatserver.exception.ChatException;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WsChatClient implements ChatClientAPI{
    private final Supplier<Message> pollMessage;
    private final Runnable onDisconnect;
    private final Consumer<Message> messageSender;

    public WsChatClient(
                        Consumer<Message> messageSender,
                        Supplier<Message> pollMessage,
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
    public Message pollMessage() throws ChatException{
        return  pollMessage.get();
    }
}
