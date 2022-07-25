package com.wu.chatserver.repository;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.ChatRoom_;
import com.wu.chatserver.domain.User;
import com.wu.chatserver.domain.User_;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.*;

@ApplicationScoped
public class ChatRoomRepository extends GenericDaoSkeletal<Long, ChatRoom> implements ChatRoomDao {

    public ChatRoomRepository() {
        super(ChatRoom.class);
    }

    @Override
    public boolean isUserMemberOfChatRoom(Long chatId, Long userId) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Object> query = criteriaBuilder.createQuery();
        Root<ChatRoom> root = query.from(ChatRoom.class);
        SetJoin<ChatRoom, User> join = root.join(ChatRoom_.members, JoinType.LEFT);
        Predicate equalChatId = criteriaBuilder.equal(root.get(ChatRoom_.ID), chatId);
        Predicate equalUserId = criteriaBuilder.equal(root.get(User_.ID), userId);
        query.select(join).where(equalChatId).where(equalUserId);
        return !em.createQuery(query).setMaxResults(1).getResultList().isEmpty();
    }
}
