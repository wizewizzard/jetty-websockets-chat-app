package com.wu.chatserver.util;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.Message;
import com.wu.chatserver.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.password.PasswordEncryptor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
@Slf4j
public class TestDataFiller {
    @Inject
    private EntityManagerFactory emf;
    @Inject
    private PasswordEncryptor passwordEncryptor;
    public void fillData(){
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        User user1 = new User();
        user1.setUserName("testUser1");
        user1.setPassword(passwordEncryptor.encryptPassword("testUser1"));
        user1.setEmail("testuser1@mail.com");
        User user2 = new User();
        user2.setUserName("testUser2");
        user2.setPassword(passwordEncryptor.encryptPassword("testUser2"));
        user2.setEmail("testuser2@mail.com");
        ChatRoom chatRoom1 = new ChatRoom();
        chatRoom1.setName("Room for both users");
        chatRoom1.addMember(user1);
        chatRoom1.addMember(user2);

        int messageNum = 114;
        LocalDateTime baseDT = LocalDateTime.of(2022, 7, 20, 0, 0, 0);

        for(int i =0; i < messageNum; i ++){
            Message message = new Message();
            message.setCreatedBy(ThreadLocalRandom.current().nextBoolean() ? user1 : user2);
            message.setBody("Message #" + (i + 1));
            chatRoom1.addMessage(message);
            baseDT = baseDT.plusMinutes(1);
            message.setPublishedAt(baseDT);
        }

        tx.begin();
        try{
            em.persist(user1);
            em.persist(user2);
            em.persist(chatRoom1);
            tx.commit();
        }
        catch (Exception e ){
            log.error("Error when filling app with data");
            tx.rollback();
        }
    }
}
