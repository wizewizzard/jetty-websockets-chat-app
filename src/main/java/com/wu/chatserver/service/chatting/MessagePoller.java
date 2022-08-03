package com.wu.chatserver.service.chatting;

@FunctionalInterface
public interface MessagePoller<T> {

    public T get() throws InterruptedException;
}
