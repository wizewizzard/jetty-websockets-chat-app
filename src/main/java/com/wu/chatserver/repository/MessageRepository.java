package com.wu.chatserver.repository;

import com.wu.chatserver.domain.*;
import com.wu.chatserver.dto.MessageDTO;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class MessageRepository extends GenericDaoSkeletal<Long, Message> implements MessageDao{
    public MessageRepository() {
        super(Message.class);
    }

    public List<MessageDTO.Response.MessageWithAuthor> findByChatBeforeDate(ChatRoom chatRoom, LocalDateTime dateTime, int pageSize){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MessageDTO.Response.MessageWithAuthor> criteriaQuery = cb.createQuery(MessageDTO.Response.MessageWithAuthor.class);
        Root<ChatRoom> root = criteriaQuery.from(ChatRoom.class);
        ListJoin<ChatRoom, Message> messagesJoin = root.join(ChatRoom_.messages);
        Join<Message, User> userJoin = messagesJoin.join(Message_.createdBy);
        criteriaQuery.select((cb.construct(
                MessageDTO.Response.MessageWithAuthor.class,
                messagesJoin.get(Message_.body),
                messagesJoin.get(Message_.publishedAt),
                userJoin.get(User_.userName)
                )));

        ParameterExpression<Long> chatId = cb.parameter(Long.class);
        ParameterExpression<LocalDateTime> dateTimeParam = cb.parameter(LocalDateTime.class);
        Predicate chatEq = cb.equal(root.get(ChatRoom_.id), chatId);
        Predicate dateUntil = cb.lessThan(messagesJoin.get(Message_.PUBLISHED_AT), dateTimeParam);

        criteriaQuery.where(cb.and(chatEq, dateUntil)).orderBy(cb.desc(messagesJoin.get(Message_.publishedAt)));
        TypedQuery<MessageDTO.Response.MessageWithAuthor> query = em.createQuery(criteriaQuery).setMaxResults(pageSize);
        query.setParameter(chatId, chatRoom.getId());
        query.setParameter(dateTimeParam, dateTime);

        return query.getResultList();
    }
}
