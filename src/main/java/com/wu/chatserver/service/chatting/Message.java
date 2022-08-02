package com.wu.chatserver.service.chatting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private Long chatId;
    private Long userId;
    private String userName;
    private String body;
    private LocalDateTime publishedAt;

    public Message(Long chatId, String body) {
        this.chatId = chatId;
        this.body = body;
    }
}
