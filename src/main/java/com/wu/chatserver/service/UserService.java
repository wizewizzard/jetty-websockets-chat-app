package com.wu.chatserver.service;

import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.domain.User;
import com.wu.chatserver.domain.UsersChatSession;
import com.wu.chatserver.dto.TokenDTO;
import com.wu.chatserver.dto.UserDTO;
import com.wu.chatserver.exception.AuthenticationException;
import com.wu.chatserver.exception.RegistrationException;
import com.wu.chatserver.jwtauth.JwtManager;
import com.wu.chatserver.repository.ChatRoomDao;
import com.wu.chatserver.repository.UserDao;
import com.wu.chatserver.repository.UsersChatSessionDao;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.password.PasswordEncryptor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
@NoArgsConstructor
@Slf4j
public class UserService {
    private PasswordEncryptor passwordEncryptor;
    private EntityManager em;
    private UserDao userDao;
    private ChatRoomDao chatRoomDao;

    private UsersChatSessionDao sessionDao;
    private JwtManager jwtManager;

    @Inject
    public UserService(EntityManager em,
                       PasswordEncryptor passwordEncryptor,
                       UserDao userDao,
                       ChatRoomDao chatRoomDao,
                       UsersChatSessionDao sessionDao,
                       JwtManager jwtManager) {
        this.passwordEncryptor = passwordEncryptor;
        this.userDao = userDao;
        this.chatRoomDao = chatRoomDao;
        this.sessionDao = sessionDao;
        this.em = em;
        this.jwtManager = jwtManager;
    }

    public void registerUser(UserDTO.Request.Registration userRegistrationDTO) {
        if (!userDao.uniqueUserNameAndEmail(userRegistrationDTO.getUserName(), userRegistrationDTO.getEmail())) {
            throw new RegistrationException("User with specified user name or email already exists");
        }

        User user = new User();
        user.setUserName(userRegistrationDTO.getUserName());
        user.setPassword(passwordEncryptor.encryptPassword(userRegistrationDTO.getPassword()));
        user.setEmail(userRegistrationDTO.getEmail());

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            userDao.save(user);
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            throw new RuntimeException("Transaction was not successful");
        }
    }

    public String loginUser(UserDTO.Request.Login loginDto) {
        User user = userDao.findUserByUserName(loginDto.getUserName())
                .orElseThrow(() -> new AuthenticationException("Wrong credentials"));
        if (!passwordEncryptor.checkPassword(loginDto.getPassword(), user.getPassword()))
            throw new AuthenticationException("Wrong credentials");
        return jwtManager.generate(Map.of("userId", user.getId(), "userName", user.getUserName()));
    }

    public UserDTO.Response.UserInfo verifyToken(TokenDTO tokenDTO) {
        Jws<Claims> claimsJws = jwtManager.parse(tokenDTO.getToken());
        Long userId = claimsJws.getBody().get("userId", Long.class);
        User user = userDao.findById(userId).orElseThrow(() -> new AuthenticationException("Invalid token"));
        return new UserDTO.Response.UserInfo(user.getId(), user.getUserName());

    }

    public Optional<User> getUserByUserName(String userName) {
        return userDao.findUserByUserName(userName);
    }

    public void setUserOnlineStatusForRoom(Long chatRoomId, Long userId, UsersChatSession.OnlineStatus onlineStatus) {
        log.info("Setting {} status for user {} in room {}", onlineStatus, userId, chatRoomId);
        ChatRoom chatRoom = chatRoomDao.findById(chatRoomId).orElseThrow();
        User user = userDao.findById(userId).orElseThrow();
        if (chatRoomDao.isUserMemberOfChatRoom(chatRoom, user)) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                sessionDao.setOnlineStatus(chatRoom, user, onlineStatus, LocalDateTime.now());
                tx.commit();
            } catch (Throwable e) {
                tx.rollback();
                throw new RuntimeException("Transaction was not successful");
            }
        }
    }

    public void userSetOnlineStatus(Long userId, UsersChatSession.OnlineStatus onlineStatus) {
        log.info("Setting {} status for user {}", onlineStatus, userId);
        User user = userDao.findById(userId).orElseThrow();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (onlineStatus.equals(UsersChatSession.OnlineStatus.ONLINE))
                sessionDao.setUserOnline(user, LocalDateTime.now());
            else
                sessionDao.setUserOffline(user, LocalDateTime.now());
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            throw new RuntimeException("Transaction was not successful");
        }

    }
}
