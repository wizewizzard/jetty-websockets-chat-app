package com.wu.chatserver.repository;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.ChatRoom_;
import com.wu.chatserver.domain.User;
import com.wu.chatserver.domain.User_;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

@ApplicationScoped
public class ChatRoomRepository extends GenericDaoSkeletal<Long, ChatRoom> implements ChatRoomDao {

    public ChatRoomRepository() {
        super(ChatRoom.class);
    }

    @Override
    public boolean isUserMemberOfChatRoom(ChatRoom chatRoom, User user) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object> criteriaQuery = cb.createQuery();

        Root<ChatRoom> root = criteriaQuery.from(ChatRoom.class);
        SetJoin<ChatRoom, User> userJoin = root.join(ChatRoom_.members, JoinType.LEFT);

        ParameterExpression<Long> chatId = cb.parameter(Long.class);
        Predicate equalChatId = cb.equal(root.get(ChatRoom_.ID), chatId);
        ParameterExpression<Long> userId = cb.parameter(Long.class);
        Predicate equalUserId = cb.equal(userJoin.get(User_.ID), userId);

        criteriaQuery.select(root).where(cb.and(equalChatId, equalUserId));
        TypedQuery<Object> query = em.createQuery(criteriaQuery).setMaxResults(1);
        query.setParameter(chatId, chatRoom.getId());
        query.setParameter(userId, user.getId());
        return !query.getResultList().isEmpty();
    }
}
