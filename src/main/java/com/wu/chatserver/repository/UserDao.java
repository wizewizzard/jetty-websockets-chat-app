package com.wu.chatserver.repository;

import com.wu.chatserver.domain.User;

import java.util.Optional;

public interface UserDao extends GenericDao<Long, User> {
    Optional<User> findUserByUserName(String userName);

    boolean uniqueUserNameAndEmail(String userName, String email);

    Optional<User> findUserWithChatRoomsByUserName(String userName);
}
