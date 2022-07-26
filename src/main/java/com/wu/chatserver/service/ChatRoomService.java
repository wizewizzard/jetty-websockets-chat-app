package com.wu.chatserver.service;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.UsersChatSession;
import com.wu.chatserver.dto.ChatRoomDTO;

import java.util.Optional;

public interface ChatRoomService {
    public ChatRoom createChatRoom(ChatRoomDTO.Request.Create createDto, Long chatRoomId);
    public void deleteChatRoom(Long chatRoomId, Long userId);
    public Optional<ChatRoom> findChatRoomById(Long chatRoomId);
    public void addUserToChat(Long chatRoomId, Long userId);
    public void removeUserFromChat(Long chatRoomId, Long id);
}
