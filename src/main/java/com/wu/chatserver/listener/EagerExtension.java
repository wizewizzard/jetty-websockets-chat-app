package com.wu.chatserver.listener;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.persistence.EntityManagerFactory;

@Slf4j
public class EagerExtension implements Extension {
    public void load(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
        log.debug("Initializing eager beans");
        EntityManagerFactory entityManagerFactory = CDI.current().select(EntityManagerFactory.class).get();
        entityManagerFactory.toString();
    }

}