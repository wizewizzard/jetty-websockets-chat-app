package com.wu.chatserver.service.chatting.event;

import com.wu.chatserver.domain.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class ChatRoomDeleted {
    private ChatRoom chatRoom;
}
