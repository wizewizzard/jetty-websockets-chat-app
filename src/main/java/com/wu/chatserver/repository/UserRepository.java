package com.wu.chatserver.repository;

import com.wu.chatserver.domain.User;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Optional;

@Slf4j
@ApplicationScoped
public class UserRepository extends GenericDaoSkeletal<Long, User> implements UserDao {
    public UserRepository() {
        super(User.class);
    }

    public Optional<User> findUserByUserName(String username) {
        TypedQuery<User> query = em.createQuery("FROM User u where u.userName=?1", User.class);
        query.setParameter(1, username);
        try{
            return Optional.of(query.getSingleResult());
        }
        catch (NoResultException noResultException){
            return Optional.empty();
        }
    }

    @Override
    public boolean uniqueUserNameAndEmail(String userName, String email) {
        TypedQuery<Boolean> query = em.createQuery("select case when (count(u.id) > 0)  then false else true end FROM User u where u.userName=?1 or u.email=?2",
                Boolean.class);
        query.setParameter(1, userName);
        query.setParameter(2, email);
        return query.getSingleResult();
    }
}
