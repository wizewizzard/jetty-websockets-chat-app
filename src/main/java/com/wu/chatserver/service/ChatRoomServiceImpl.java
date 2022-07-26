package com.wu.chatserver.service;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.User;
import com.wu.chatserver.domain.UsersChatSession;
import com.wu.chatserver.dto.ChatRoomDTO;
import com.wu.chatserver.exception.NotEnoughRightsException;
import com.wu.chatserver.repository.ChatRoomRepository;
import com.wu.chatserver.repository.UserRepository;
import lombok.NoArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
@NoArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService{
    private ChatRoomRepository chatRoomRepository;
    private UserRepository userRepository;
    private EntityManager em;

    public ChatRoomServiceImpl(ChatRoomRepository chatRoomRepository,
                               UserRepository userRepository,
                               EntityManager em) {
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
        this.em = em;
    }

    @Override
    public ChatRoom createChatRoom(ChatRoomDTO.Request.Create createDto, Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        User user = optionalUser.orElseThrow();
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setCreatedBy(user);
        chatRoom.addMember(user);
        chatRoom.setName(createDto.getChatName());
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            chatRoomRepository.save(chatRoom);
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            throw new RuntimeException("Transaction was not successful");
        }
        return chatRoom;
    }

    @Override
    public void deleteChatRoom(Long chatRoomId, Long userId) {
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(chatRoomId);
        ChatRoom chatRoom = chatRoomOptional.orElseThrow();
        if(Objects.equals(chatRoom.getCreatedBy().getId(), userId)){
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                chatRoomRepository.remove(em.merge(chatRoom));
                tx.commit();
            } catch (Throwable e) {
                tx.rollback();
                throw new RuntimeException("Transaction was not successful");
            }
        }
        else{
            throw new NotEnoughRightsException("You do not have permission to delete this chat room");
        }
    }

    @Override
    public Optional<ChatRoom> findChatRoomById(Long chatRoomId) {
        return chatRoomRepository.findById(chatRoomId);
    }

    @Override
    public void addUserToChat(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            chatRoom.addMember(user);
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            throw new RuntimeException("Transaction was not successful");
        }
    }

    @Override
    public void removeUserFromChat(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            chatRoom.removeMember(user);
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            throw new RuntimeException("Transaction was not successful");
        }
    }

}
