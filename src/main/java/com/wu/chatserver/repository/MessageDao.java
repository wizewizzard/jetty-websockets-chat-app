package com.wu.chatserver.repository;

import com.wu.chatserver.domain.Message;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageDao extends GenericDao<Long, Message>{
    public List<Message> findByChatBeforeDate(Long chatId, LocalDateTime dateTime, int pageSize);
}
