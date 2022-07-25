package com.wu.chatserver.repository;

import java.util.List;
import java.util.Optional;

public interface GenericDao<K, T> {

    Optional<T> findById( K id);

    List<T> findAll();

    void save(T entity);

    void removeById(K id);

    void remove(T entity);

}
