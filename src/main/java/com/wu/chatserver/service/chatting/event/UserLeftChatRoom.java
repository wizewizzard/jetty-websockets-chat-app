package com.wu.chatserver.service.chatting.event;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.User;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class UserLeftChatRoom {
    private final User user;
    private final ChatRoom chatRoom;
}
