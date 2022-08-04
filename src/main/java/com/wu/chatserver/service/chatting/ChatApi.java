package com.wu.chatserver.service.chatting;

import com.wu.chatserver.exception.ChatException;

import java.util.concurrent.TimeoutException;

public class ChatApi {
    private final MessageConsumer<Message> messageConsumer;
    private final MessagePoller<Message> pollMessage;
    private final Runnable onDisconnect;

    public ChatApi(MessageConsumer<Message> messageConsumer, MessagePoller<Message> pollMessage, Runnable onDisconnect) {
        this.messageConsumer = messageConsumer;
        this.pollMessage = pollMessage;
        this.onDisconnect = onDisconnect;
    }

    public void sendMessage(Message message) throws ChatException, InterruptedException, TimeoutException {
        messageConsumer.accept(message);
    }

    public Message pollMessage() throws InterruptedException, ChatException {
        return  pollMessage.get();
    }

    public void disconnect(){
        onDisconnect.run();
    }
}
