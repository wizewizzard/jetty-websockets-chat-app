package com.wu.chatserver.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @ManyToMany
    @JoinTable(name = "chats_members",
        joinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "chat_room_id", nullable = false),
        inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    )
    @Getter
    private Set<User> members = new HashSet<>();

    @Getter
    @OneToMany(mappedBy = "chatSessionId.chatRoom", cascade ={ CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Set<UsersChatSession> usersChatSessions = new HashSet<>();

    public void addMember(User user){
        members.add(user);
        UsersChatSession chatSession = new UsersChatSession();
        chatSession.setChatSessionId(new ChatSessionId(this, user));
        usersChatSessions.add(chatSession);
    }

    public void removeMember(User user){
        members.remove(user);
        usersChatSessions.removeIf(chatSession -> chatSession.getChatSessionId().getUser().equals(user));
    }
}
