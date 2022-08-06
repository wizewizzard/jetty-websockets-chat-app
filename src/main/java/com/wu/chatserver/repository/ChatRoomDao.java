package com.wu.chatserver.repository;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.User;

import java.util.List;
import java.util.Optional;

public interface ChatRoomDao extends GenericDao<Long, ChatRoom> {
    public boolean isUserMemberOfChatRoom(ChatRoom chatRoom, User user);

    Optional<ChatRoom> findChatRoomByIdWithMembers(Long id);

    List<ChatRoom> findChatRoomsWithNameLike(String chatRoomName);
}
