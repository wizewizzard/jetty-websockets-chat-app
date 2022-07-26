package com.wu.chatserver.repository;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.Message;
import com.wu.chatserver.dto.MessageDTO;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public interface MessageDao extends GenericDao<Long, Message>{
    public List<MessageDTO.Response.MessageWithAuthor> findByChatBeforeDate(ChatRoom chatRoom, LocalDateTime dateTime, int pageSize);
}
