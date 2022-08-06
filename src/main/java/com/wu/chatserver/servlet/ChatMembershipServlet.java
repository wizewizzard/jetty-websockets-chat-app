package com.wu.chatserver.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wu.chatserver.domain.ChatRoom;
import com.wu.chatserver.dto.ChatRoomDTO;
import com.wu.chatserver.exception.RequestException;
import com.wu.chatserver.jwtauth.JwtPrincipal;
import com.wu.chatserver.service.ChatRoomService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public class ChatMembershipServlet extends HttpServlet {
    private ChatRoomService chatRoomService;

    private ObjectMapper mapper;

    @Inject
    public void setChatRoomService(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }
    
    @Inject
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.isUserInRole("user") || !(req.getUserPrincipal() instanceof JwtPrincipal))
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You have no rights to do that");
        String usedId = req.getParameter("user");
        try {
            List<ChatRoom> chatRoomsForUser = chatRoomService.findChatRoomsForUser(Long.parseLong(usedId), null);
            resp.setStatus(HttpServletResponse.SC_OK);
            PrintWriter writer = resp.getWriter();
            if (chatRoomsForUser == null || chatRoomsForUser.isEmpty()) {
                writer.write(mapper.writeValueAsString(List.of()));
            } else {
                List<ChatRoomDTO.Response.ChatRoomInfo> respDto =
                        chatRoomsForUser.stream().map(r -> new ChatRoomDTO.Response.ChatRoomInfo(r.getId(), r.getName()))
                                .collect(Collectors.toList());
                writer.write(mapper.writeValueAsString(respDto));
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.isUserInRole("user") || !(req.getUserPrincipal() instanceof JwtPrincipal))
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You have no rights to do that");
        String[] parts = req.getPathInfo().split("/");
        long chatId = Long.parseLong(parts[parts.length - 1]);
        try {
            chatRoomService.addUserToChat(chatId, ((JwtPrincipal) req.getUserPrincipal()).getUserId());
        } catch (RequestException exception) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!req.isUserInRole("user") || !(req.getUserPrincipal() instanceof JwtPrincipal))
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You have no rights to do that");
        String[] parts = req.getPathInfo().split("/");
        long chatId = Long.parseLong(parts[parts.length - 1]);
        try {
            chatRoomService.removeUserFromChat(chatId, ((JwtPrincipal) req.getUserPrincipal()).getUserId());
        } catch (RequestException exception) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }

    }
}
