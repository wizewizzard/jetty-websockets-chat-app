package com.wu.chatserver.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "chat_room")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "chatRoomSeqGen")
    @SequenceGenerator(name = "chatRoomSeqGen", sequenceName = "chat_room_id_sequence")
    @Column(name = "chat_room_id")
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private String name;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    @Getter
    @Setter
    private User createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "chats_members",
            joinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "chat_room_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    )
    @Getter
    private Set<User> members = new HashSet<>();

    @Getter
    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages = new ArrayList<>();

    @Getter
    @OneToMany(mappedBy = "chatSessionId.chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UsersChatSession> usersChatSessions = new HashSet<>();

    public void addMember(User user) {
        members.add(user);
        user.getChatRooms().add(this);
        UsersChatSession chatSession = new UsersChatSession();
        chatSession.setChatSessionId(new ChatSessionId(this, user));
        user.getChatSessions().add(chatSession);
        usersChatSessions.add(chatSession);
    }

    public void removeMember(User user) {
        members.remove(user);
        user.getChatRooms().remove(this);
        //usersChatSessions.removeIf(chatSession -> chatSession.getChatSessionId().getUser().equals(user));
    }

    public void addMessage(Message message) {
        messages.add(message);
        message.setChatRoom(this);
    }

    public void removeMessage(Message message) {
        messages.remove(message);
        message.setChatRoom(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatRoom chatRoom = (ChatRoom) o;
        return name.equals(chatRoom.name) && createdBy.equals(chatRoom.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, createdBy);
    }
}
