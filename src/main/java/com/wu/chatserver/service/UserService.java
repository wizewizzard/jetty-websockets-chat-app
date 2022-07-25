package com.wu.chatserver.service;

import com.wu.chatserver.domain.User;
import com.wu.chatserver.dto.UserDTO;
import com.wu.chatserver.exception.AuthenticationException;
import com.wu.chatserver.exception.RegistrationException;
import com.wu.chatserver.jwtauth.JwtManager;
import com.wu.chatserver.repository.UserDao;
import lombok.NoArgsConstructor;
import org.jasypt.util.password.PasswordEncryptor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
@NoArgsConstructor
public class UserService {
    private PasswordEncryptor passwordEncryptor;
    private EntityManager em;
    private UserDao userRepository;
    private JwtManager jwtManager;

    @Inject
    public UserService(EntityManager em,
                       PasswordEncryptor passwordEncryptor,
                       UserDao userRepository,
                       JwtManager jwtManager) {
        this.passwordEncryptor = passwordEncryptor;
        this.userRepository = userRepository;
        this.em = em;
        this.jwtManager = jwtManager;
    }

    public void registerUser(UserDTO.Request.Registration userRegistrationDTO) {
        if(!userRepository.uniqueUserNameAndEmail(userRegistrationDTO.getUserName(), userRegistrationDTO.getEmail())){
            throw new RegistrationException("User with specified user name or email already exists");
        }

        User user = new User();
        user.setUserName(userRegistrationDTO.getUserName());
        user.setPassword(passwordEncryptor.encryptPassword(userRegistrationDTO.getPassword()));
        user.setEmail(userRegistrationDTO.getEmail());

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            userRepository.save(user);
            tx.commit();
        } catch (Throwable e) {
            tx.rollback();
            throw new RuntimeException("Transaction was not successful");
        }
    }

    public String loginUser(UserDTO.Request.Login loginDto) {
        User user = userRepository.findUserByUserName(loginDto.getUserName())
                .orElseThrow(() -> new AuthenticationException("Wrong credentials"));
        if(!passwordEncryptor.checkPassword(loginDto.getPassword(), user.getPassword()))
            throw  new AuthenticationException("Wrong credentials");
        return jwtManager.generate(Map.of("userId", user.getId(),"userName", user.getUserName()));
    }
}
