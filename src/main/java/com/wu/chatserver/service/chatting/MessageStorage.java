package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import com.wu.chatserver.service.MessageService;
import com.wu.chatserver.service.chatting.event.MessageReceived;
import lombok.extern.slf4j.Slf4j;
import org.jboss.weld.context.activator.ActivateRequestContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@ApplicationScoped
public class MessageStorage {
    @Inject
    private MessageService messageService;

    @ActivateRequestContext
    public void messageReceived(@Observes MessageReceived messageReceived){
        try{
            log.debug("Message storing");
            Message message = messageReceived.getMessage();
            User user = messageReceived.getUser();
            messageService.publishMessage(message.getChatId(),user.getId(), message.getBody());
        }
        catch (Exception e){
            log.error("Unable to store a message", e);
        }
    }
}
