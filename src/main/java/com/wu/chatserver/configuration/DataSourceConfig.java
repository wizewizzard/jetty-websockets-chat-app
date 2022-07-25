package com.wu.chatserver.configuration;


import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

@Slf4j
@ApplicationScoped
public class DataSourceConfig {
    @Produces
    @Default
    @ApplicationScoped
    public EntityManagerFactory entityManagerFactory() {
        log.debug("EntityManagerFactory bean creation");
        return Persistence.createEntityManagerFactory("chat_persistence_unit");
    }

    public void closeEntityManagerFactory(@Disposes EntityManagerFactory entityManagerFactory) {
        entityManagerFactory.close();
    }

    @Produces
    @RequestScoped
    public EntityManager createEntityManager(EntityManagerFactory entityManagerFactory) {
        log.debug("EntityManager bean creation");
        return entityManagerFactory.createEntityManager();
    }

    public void closeEntityManager(@Disposes EntityManager em) {
        log.debug("EntityManager closing");
        em.close();
    }
}
