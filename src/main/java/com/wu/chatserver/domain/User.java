package com.wu.chatserver.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}
