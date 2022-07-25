package com.wu.chatserver.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_chat_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersChatSession {
    public enum OnlineStatus{
        UNKNOWN, ONLINE, OFFLINE
    }
    @EmbeddedId
    private ChatSessionId chatSessionId = new ChatSessionId();

    private LocalDateTime startedAt;

    @Column(nullable = true)
    private LocalDateTime endedAt;
}
