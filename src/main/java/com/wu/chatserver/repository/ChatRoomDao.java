package com.wu.chatserver.repository;

import com.wu.chatserver.domain.ChatRoom;

public interface ChatRoomDao extends GenericDao<Long, ChatRoom> {
    public boolean isUserMemberOfChatRoom(Long chatId, Long userId);
}
