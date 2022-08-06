//
// ========================================================================
// Copyright (c) Webtide LLC and others.
//
// This program and the accompanying materials are made available under
// the terms of the Eclipse Public License 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0
//
// This Source Code may also be made available under the following
// Secondary Licenses when the conditions for such availability set
// forth in the Eclipse Public License, v. 2.0 are satisfied:
// the Apache License v2.0 which is available at
// https://www.apache.org/licenses/LICENSE-2.0
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package com.wu.chatserver.websocket;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wu.chatserver.exception.ChatException;
import com.wu.chatserver.jwtauth.JwtManager;
import com.wu.chatserver.service.chatting.ChatClientAPI;
import com.wu.chatserver.service.chatting.ChatRoomRealm;
import com.wu.chatserver.service.chatting.Message;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@ServerEndpoint("/wssocket/chat")
@Slf4j
public class ChatSocket {
    private Session session;
    @Inject
    private ChatRoomRealm chatRoomRealm;
    @Inject
    private ObjectMapper mapper;

    @Inject
    private JwtManager jwtManager;
    private Thread pollingThread;
    private ChatClientAPI chatClientAPI;

    public ChatSocket() {

    }

    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;
        log.info("onOpen() session");
        Map<String, List<String>> parameterMap = session.getRequestParameterMap();
        if (parameterMap.get("token") == null || parameterMap.get("token").isEmpty()) {
            this.session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthenticated."));
        }
        String token = parameterMap.get("token").get(0);
        Jws<Claims> claimsJws;
        try {
            claimsJws = jwtManager.parse(token);
        } catch (Exception e) {
            this.session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Malformed token."));
            return;
        }
        log.info("Username is {}", claimsJws.getBody().get("userName", String.class));
        chatClientAPI = new ChatClientAPI(chatRoomRealm);
        try {
            chatClientAPI.connect(() -> claimsJws.getBody().get("userName", String.class));
            pollingThread = new Thread(() -> {
                log.info("Client's polling thread started");
                while (!Thread.interrupted()) {
                    try {
                        Message message = chatClientAPI.pollMessage();
                        log.info("MMMM {}", message);
                        this.session.getBasicRemote().sendText(mapper.writeValueAsString(message));
                    } catch (InterruptedException | IOException e) {
                        log.info("Client's polling thread must be terminated because of", e);
                        try {
                            this.session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Closing on error"));
                        } catch (IOException ignored) {
                        }
                        break;
                    }
                }
                log.info("Client's polling thread is over");
            });
            pollingThread.start();
        } catch (ChatException e) {
            this.session.close(new CloseReason(CloseReason.CloseCodes.TRY_AGAIN_LATER, e.getMessage()));
        }
    }

    @OnMessage
    public void onMessage(String msg) throws IOException {
        log.info("Message received: " + msg);
        try {
            Message message = mapper.readValue(msg, Message.class);
            chatClientAPI.sendMessage(message);
        } catch (JsonParseException e) {
            log.warn("Wrong message format");
            this.session.getBasicRemote().sendText("Wrong message format");
        } catch (ChatException exception) {
            this.session.getBasicRemote().sendText("Error: " + exception.getMessage());
        } catch (InterruptedException | TimeoutException e) {
            log.warn("Unable to send message");
            this.session.getBasicRemote().sendText("Unable to send message");
        } catch (Exception e) {
            log.error("Error occurred", e);
            this.session.getBasicRemote().sendText("Error occurred");
        }
        /*try{
            chatRoom.sendMessage();
        }
        catch (ChatException exception){
            chatRoom.disconnect();
            this.session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, exception.getMessage()));
        }*/
    }

    @OnClose
    public void onClose(CloseReason close) throws IOException {
        log.info("onClose() close:" + close);
        chatClientAPI.disconnect();
        this.session.close(close);
    }

    @OnError
    public void onError(Throwable t) throws IOException {
        log.info("onError()", t);
        chatClientAPI.disconnect();
        this.session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, "Closing on error"));
    }
}
