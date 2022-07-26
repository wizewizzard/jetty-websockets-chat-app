package com.wu.chatserver.repository;

import com.wu.chatserver.domain.*;
import com.wu.chatserver.dto.UserDTO;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UsersChatSessionRepository extends GenericDaoSkeletal<ChatSessionId, UsersChatSession> implements UsersChatSessionDao{
    public UsersChatSessionRepository() {
        super(UsersChatSession.class);
    }

    @Override
    public void setOnlineStatus(ChatRoom chatRoom,
                                User user,
                                UsersChatSession.OnlineStatus onlineStatus,
                                LocalDateTime updateDate) {

        Optional<UsersChatSession> sessionOptional = this.findById(new ChatSessionId(chatRoom, user));
        UsersChatSession chatSession;
        if(sessionOptional.isPresent()){
            chatSession = sessionOptional.get();
        }
        else{
            chatSession = new UsersChatSession();
            chatSession.setChatSessionId(new ChatSessionId(chatRoom, user));
        }

        if(onlineStatus.equals(UsersChatSession.OnlineStatus.ONLINE)){
            chatSession.setStartedAt(updateDate);
            chatSession.setEndedAt(null);
        }
        else{
            chatSession.setEndedAt(updateDate);
        }
        em.persist(chatSession);
    }

    @Override
    public List<UserDTO.Response.UserOnlineStatus> getUsersOnlineStatusForChatRoom(ChatRoom chatRoom) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);
        Root<ChatRoom> root = query.from(ChatRoom.class);
        SetJoin<ChatRoom, User> members = root.join(ChatRoom_.members, JoinType.LEFT);
        SetJoin<User, UsersChatSession> chatSessions = members.join(User_.chatSessions, JoinType.LEFT);

        query.multiselect(
                root.get(User_.ID).alias("userId"),
                members.get(User_.USER_NAME).alias("userName"),
                chatSessions.get(UsersChatSession_.STARTED_AT).alias("startedAt"),
                chatSessions.get(UsersChatSession_.ENDED_AT).alias("endedAt")
        );

        ParameterExpression<Long> chatId = cb.parameter(Long.class);
        Predicate chatEq = cb.equal(root.get(ChatRoom_.id), chatId);

        query.where(chatEq);
        List<Tuple> tuples = em.createQuery(query).setParameter(chatId, chatRoom.getId()).getResultList();
        List<UserDTO.Response.UserOnlineStatus> userOnlineStatuses = new ArrayList<>();
        tuples.forEach(tuple -> {
            UserDTO.Response.UserOnlineStatus userOnlineStatus = new UserDTO.Response.UserOnlineStatus();
            userOnlineStatus.setId(tuple.get("userId", Long.class));
            userOnlineStatus.setUserName(tuple.get("userName", String.class));
            userOnlineStatus.setStartedAt(tuple.get("startedAt", LocalDateTime.class));
            userOnlineStatus.setEndedAt(tuple.get("endedAt", LocalDateTime.class));
            userOnlineStatuses.add(userOnlineStatus);
        });

        return userOnlineStatuses;
    }
}
