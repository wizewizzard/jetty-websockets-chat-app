package com.wu.chatserver.service.chatting.event;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class ChatRoomCreated {
    private final User createdBy;
    private final ChatRoom chatRoom;
}
