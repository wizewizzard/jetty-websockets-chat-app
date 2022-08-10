package com.wu.chatserver.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wu.chatserver.dto.ChatRoomDTO;
import com.wu.chatserver.service.ChatRoomService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ChatSearchServlet extends HttpServlet {
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
        String name = req.getParameter("name");
        if(name == null || name.isEmpty()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Name must not be blank");
            return;
        }
        List<ChatRoomDTO.Response.ChatRoomInfo> chatRoomsByName = chatRoomService.findChatRoomsByName(name);
        resp.setContentType("application/json; charset=utf-8");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter writer = resp.getWriter();
        writer.write(mapper.writeValueAsString(chatRoomsByName));
    }
}
