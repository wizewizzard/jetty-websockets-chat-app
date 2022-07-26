package com.wu.chatserver.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_chat_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersChatSession {
    public enum OnlineStatus {
        UNKNOWN, ONLINE, OFFLINE
    }

    @EmbeddedId
    private ChatSessionId chatSessionId = new ChatSessionId();

    private LocalDateTime startedAt;

    @Column(nullable = true)
    private LocalDateTime endedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsersChatSession that = (UsersChatSession) o;
        return chatSessionId.equals(that.chatSessionId);
    }

    @Override
    public int hashCode() {
        return chatSessionId.hashCode();
    }
}
