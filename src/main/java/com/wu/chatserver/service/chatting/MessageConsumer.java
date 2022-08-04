package com.wu.chatserver.service.chatting;

import java.util.concurrent.TimeoutException;

@FunctionalInterface
public interface MessageConsumer<T> {
    void accept(T msg) throws InterruptedException, TimeoutException;
}
