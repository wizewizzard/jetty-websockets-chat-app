package com.wu.chatserver.repository;

import com.wu.chatserver.domain.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class MessageRepository extends GenericDaoSkeletal<Long, Message> implements MessageDao{
    public MessageRepository() {
        super(Message.class);
    }

    public List<Message> findByChatBeforeDate(Long chatId, LocalDateTime dateTime, int pageSize){
        TypedQuery<Message> namedQuery = em.createNamedQuery("Message.findByChatAndDate", Message.class);
        namedQuery.setMaxResults(pageSize);
        namedQuery.setParameter(1, chatId);
        namedQuery.setParameter(2, dateTime);
        return namedQuery.getResultList();
    }
}
