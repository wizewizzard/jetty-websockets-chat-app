package com.wu.chatserver.repository;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.ChatSessionId;
import com.wu.chatserver.domain.User;
import com.wu.chatserver.domain.UsersChatSession;
import com.wu.chatserver.dto.ChatMembersOnlineStatusesDTO;
import com.wu.chatserver.dto.UserDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface UsersChatSessionDao extends GenericDao<ChatSessionId, UsersChatSession> {
    void setOnlineStatus(ChatRoom chatRoom, User user, UsersChatSession.OnlineStatus onlineStatus, LocalDateTime updateDate);

    void setUserOnline(User user, LocalDateTime updateDate);
    void setUserOffline(User user, LocalDateTime updateDate);

    /**
     * Returns list of DTOs where user id, name, session start and session end are specified.
     * Called is free to treat untouched OnlineStatus field as he wants. It is not DAO's responsibility to
     * set value for this field.
     * @param chatRoom chat room id
     * @return $UserOnlineStatus
     */
    List<UserDTO.Response.UserOnlineStatus> getUsersOnlineStatusForChatRoom(ChatRoom chatRoom);
}
