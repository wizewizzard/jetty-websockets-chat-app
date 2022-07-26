package com.wu.chatserver.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "chat_user")
public class User {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userSeqGen")
    @SequenceGenerator(name = "userSeqGen", sequenceName = "user_id_sequence")
    @Getter
    @Setter
    private Long id;
    @Column(name = "user_name")
    @Getter
    @Setter
    private String userName;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private String email;

    @Getter
    @OneToMany(mappedBy = "chatSessionId.user", cascade = CascadeType.REMOVE)
    private Set<UsersChatSession> chatSessions = new HashSet<>();

    @Getter
    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private List<ChatRoom> chatRooms = new ArrayList<>();
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userName.equals(user.userName) && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, email);
    }
}
