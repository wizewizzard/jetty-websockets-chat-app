package com.wu.chatserver.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wu.chatserver.dto.ChatRoomDTO;
import com.wu.chatserver.exception.NotEnoughRightsException;
import com.wu.chatserver.jwtauth.JwtPrincipal;
import com.wu.chatserver.service.ChatRoomService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ChatManagementServlet extends HttpServlet {

    private ChatRoomService chatRoomService;
    private ObjectMapper mapper = new ObjectMapper();

    @Inject
    public void setChatRoomService(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }
    @Inject
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ChatRoomDTO.Request.Create create = mapper.readValue(req.getReader(), ChatRoomDTO.Request.Create.class);
        if(create.getChatName() == null || create.getChatName().isBlank())
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name must not be blank");

        if(!req.isUserInRole("user") || !(req.getUserPrincipal() instanceof JwtPrincipal))
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You have no rights to do that");

        chatRoomService.createChatRoom(create, ((JwtPrincipal) req.getUserPrincipal()).getUserId());
    }

    /*@Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String chatIdStr = req.getParameter("chatId");
        if(chatIdStr == null || chatIdStr.isBlank())
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Chat id must not be blank");
        long chatId;
        try{
            chatId = Long.parseLong(chatIdStr);
        }
        catch (NumberFormatException exception){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wring format");
            return;
        }
        if(chatId <= 0 )
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Chat id must be greater than 0");

        if(!req.isUserInRole("user") || !(req.getUserPrincipal() instanceof JwtPrincipal))
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You have no rights to do that");

        try{
            //chatRoomService.deleteChatRoom(((JwtPrincipal) req.getUserPrincipal()).getUserId());
        }
        catch (NotEnoughRightsException exception){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }

    }*/


}
