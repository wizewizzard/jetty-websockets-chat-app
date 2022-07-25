package com.wu.chatserver.repository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public abstract class GenericDaoSkeletal<K, T> implements GenericDao<K, T> {
    protected EntityManager em;
    private final Class<T> entityClass;

    @Inject
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
    public GenericDaoSkeletal(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Optional<T> findById(K id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    @Override
    public List<T> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = cb.createQuery(entityClass);
        Root<T> root = criteriaQuery.from(entityClass);
        criteriaQuery.select(root);
        return em.createQuery(criteriaQuery).getResultList();
    }

    @Override
    public void save(T entity) {
        em.persist(entity);
    }

    @Override
    public void removeById(K id) {
        em.find(entityClass, id);
    }
}
