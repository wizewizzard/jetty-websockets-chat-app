package com.wu.chatserver.service;

import com.wu.chatserver.dto.MessageDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageService {
    public void publishMessage(Long chatRoomId, Long userId, String body);

    public List<MessageDTO.Response.MessageWithAuthor> getMessageHistory(Long chatRoomId, LocalDateTime until, int depth);

}
