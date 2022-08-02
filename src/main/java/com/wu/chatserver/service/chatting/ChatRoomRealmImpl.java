package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import com.wu.chatserver.exception.ChatException;
import com.wu.chatserver.service.ChatRoomService;
import com.wu.chatserver.service.MessageService;
import com.wu.chatserver.service.UserService;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatRoomRealmImpl implements ChatRoomRealm{
    private List<User> users = Collections.synchronizedList(new ArrayList<>());
    private List<ChatRoom> chatRooms = new CopyOnWriteArrayList<>();

    private UserService userService;
    private ChatRoomService chatRoomService;

    @Inject
    public ChatRoomRealmImpl(UserService userService, ChatRoomService chatRoomService) {
        this.userService = userService;
        this.chatRoomService = chatRoomService;
    }

    @Override
    public MessageRouter tryConnect(String userName) throws ChatException {
        //allows users to have multiple connections opened
        //update chat sessions!
        User user = userService.getUserByUserName(userName)
                .orElseThrow(() -> new ChatException("No user with given credentials found"));
        return new MessageRouterImpl(user, this);
    }

    @Override
    public void disconnect(String userName) {
        //update sessions if this is the last user's connection!
    }

    @Override
    public Optional<ChatRoom> getChatRoom(Long chatRoomId) {
        return null;
    }

    /**
     * If ws connection is opened adds user connection to the room.
     * If user did it without connection opened, does nothing
     * @param event
     */
    public void userBecameMemberOfChat(@Observes Object event){

    }

    /**
     * If ws connection is opened removes connection from room.
     * If user did it without connection opened, does nothing
     * @param event
     */
    public void userLeftChat(@Observes Object event){

    }
}
