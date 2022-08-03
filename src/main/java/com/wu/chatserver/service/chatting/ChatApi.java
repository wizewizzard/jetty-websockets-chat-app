package com.wu.chatserver.service.chatting;

import com.wu.chatserver.exception.ChatException;

import java.util.function.Consumer;

public class ChatApi {
    private final Consumer<Message> messageSender;
    private final MessagePoller<Message> pollMessage;
    private final Runnable onDisconnect;


    public ChatApi(Consumer<Message> messageSender, MessagePoller<Message> pollMessage, Runnable onDisconnect) {
        this.messageSender = messageSender;
        this.pollMessage = pollMessage;
        this.onDisconnect = onDisconnect;
    }

    public void sendMessage(Message message) throws ChatException{
        messageSender.accept(message);
    }

    public Message pollMessage() throws InterruptedException, ChatException {
        return  pollMessage.get();
    }

    public void disconnect(){
        onDisconnect.run();
    }
}
