package com.wu.chatserver.service;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.Message;
import com.wu.chatserver.domain.User;
import com.wu.chatserver.dto.MessageDTO;
import com.wu.chatserver.exception.NotEnoughRightsException;
import com.wu.chatserver.repository.ChatRoomRepository;
import com.wu.chatserver.repository.MessageRepository;
import com.wu.chatserver.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
public class MessageServiceImpl implements MessageService {

    private ChatRoomRepository chatRoomRepository;
    private UserRepository userRepository;
    private MessageRepository messageRepository;
    private EntityManager em;
    @Inject
    public MessageServiceImpl(ChatRoomRepository chatRoomRepository,
                              UserRepository userRepository,
                              MessageRepository messageRepository,
                              EntityManager em) {
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.em = em;
    }

    @Override
    public void publishMessage(Long chatRoomId, Long userId, String body) {
        log.trace("Publishing message");
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        ZonedDateTime zonedDateTime = LocalDateTime.now()
                .atZone(ZoneId.systemDefault());
        LocalDateTime utc = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        if(chatRoomRepository.isUserMemberOfChatRoom(chatRoom, user)){
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                chatRoom = em.merge(chatRoom);
                Message message = new Message();
                message.setCreatedBy(user);
                message.setPublishedAt(utc);
                message.setBody(body);
                chatRoom.addMessage(message);
                chatRoomRepository.save(chatRoom);
                tx.commit();
            } catch (Throwable e) {
                tx.rollback();
                throw new RuntimeException("Transaction was not successful");
            }
        }
        else{
            throw new NotEnoughRightsException("You are not a part of the chat");
        }
    }

    @Override
    public List<MessageDTO.Response.MessageWithAuthor> getMessageHistory(Long chatRoomId, LocalDateTime until, int depth) {
        log.trace("Requested message message");
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();
        return messageRepository.findByChatBeforeDate(chatRoom, until, depth);
    }
}
