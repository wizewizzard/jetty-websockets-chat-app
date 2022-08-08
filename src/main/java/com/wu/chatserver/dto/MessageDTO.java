package com.wu.chatserver.dto;

import lombok.*;

import java.time.LocalDateTime;

public enum MessageDTO {
    ;

    private interface Id{ Long getId(); }
    private interface Body{ String getBody();}
    private interface PublishedAt{LocalDateTime getPublishedAt();}
    private interface CreatedBy{ String getCreatedBy(); }
    private interface ChatId{ Long getChatId(); }

    public enum Request{
        ;

    }
    public enum Response{
        ;
        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        public static class MessageWithAuthor implements Body, PublishedAt, CreatedBy, ChatId{
            private String body;
            private LocalDateTime publishedAt;
            private String createdBy;
            private Long chatId;
        }
    }
}
