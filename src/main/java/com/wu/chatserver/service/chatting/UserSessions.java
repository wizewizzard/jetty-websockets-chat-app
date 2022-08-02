package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import com.wu.chatserver.dto.MessageDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.websocket.Session;
import java.util.List;

@Getter
@Setter

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class UserSessions implements RoomMember{
    @EqualsAndHashCode.Include
    private User user;

    private List<Session> sessions;

    public UserSessions(User user) {
        this.user = user;
    }

    public void addSession(Session session){

    }

    @Override
    public void closeConnection() {

    }

    @Override
    public void handleMessage(MessageDTO.Response.MessageWithAuthor message) {
        for (Session session: sessions
             ) {
            session.getBasicRemote().sendObject(message);
        }
    }
}
