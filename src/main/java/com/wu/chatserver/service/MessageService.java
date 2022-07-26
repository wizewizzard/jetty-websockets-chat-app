package com.wu.chatserver.service;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageService {
    public void publishMessage(Long chatRoomId, Long userId, String body);

    public List<?> getMessageHistory(Long chatRoomId, LocalDateTime until, int depth);

}
