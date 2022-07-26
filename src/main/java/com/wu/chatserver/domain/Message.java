package com.wu.chatserver.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@NamedQuery(name = "Message.findByChatAndDate", query = "FROM Message m WHERE m.chatRoom.id=?1 and m.publishedAt < ?2 ORDER BY m.publishedAt")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "messageSeqGen")
    @SequenceGenerator(name = "messageSeqGen", sequenceName = "message_id_sequence")
    @Getter
    @Setter
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Getter
    @Setter
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    @Getter
    @Setter
    private ChatRoom chatRoom;

    @Getter
    @Setter
    private LocalDateTime publishedAt;

    @Column(length = 1024 * 8)
    @Getter
    @Setter
    private String body;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return createdBy.equals(message.createdBy)
                && chatRoom.equals(message.chatRoom)
                && publishedAt.equals(message.publishedAt)
                && body.equals(message.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdBy, chatRoom, publishedAt, body);
    }
}
