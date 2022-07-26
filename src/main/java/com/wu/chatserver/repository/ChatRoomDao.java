package com.wu.chatserver.repository;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.User;

public interface ChatRoomDao extends GenericDao<Long, ChatRoom> {
    public boolean isUserMemberOfChatRoom(ChatRoom chatRoom, User user);
}
