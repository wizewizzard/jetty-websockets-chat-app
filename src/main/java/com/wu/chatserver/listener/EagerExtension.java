package com.wu.chatserver.listener;

import com.wu.chatserver.service.chatting.ChatRoomRealm;
import com.wu.chatserver.util.TestDataFiller;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.Extension;
import javax.persistence.EntityManagerFactory;
import java.util.Properties;

@Slf4j
public class EagerExtension implements Extension {
    public void load(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
        log.debug("Initializing eager beans");
        Properties props = CDI.current().select(Properties.class).get();
        EntityManagerFactory entityManagerFactory = CDI.current().select(EntityManagerFactory.class).get();
        entityManagerFactory.toString();
        ChatRoomRealm chatRoomRealm = CDI.current().select(ChatRoomRealm.class).get();
        chatRoomRealm.init(props);
        TestDataFiller testDataFiller = CDI.current().select(TestDataFiller.class).get();
        testDataFiller.fillData();
    }

}