package com.wu.chatserver.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wu.chatserver.dto.MessageDTO;
import com.wu.chatserver.service.ChatRoomService;
import com.wu.chatserver.service.MessageService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MessageHistoryServlet extends HttpServlet {

    private ChatRoomService chatRoomService;
    private MessageService messageService;

    private ObjectMapper mapper;

    @Inject
    public void setChatRoomService(ChatRoomService chatRoomService) {
        this.chatRoomService = chatRoomService;
    }
    @Inject
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
    @Inject
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String chatIdStr = req.getParameter("chat");
        String dateStr = req.getParameter("date");
        String depthStr = req.getParameter("depth");
        long chatId;
        if(chatIdStr == null || chatIdStr.isEmpty()){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Chat must not be blank");
            return;
        }
        try{
            chatId = Long.parseLong(chatIdStr);
            LocalDateTime untilDT = LocalDateTime.now();
            int depth = 20;
            if(dateStr != null && !dateStr.isEmpty()){
                long tstmp = Long.parseLong(dateStr);
                untilDT = LocalDateTime.ofInstant(Instant.ofEpochSecond(tstmp), ZoneId.systemDefault());
            }
            if(depthStr != null && !depthStr.isEmpty()){
                depth = Integer.parseInt(depthStr);
            }
            List<MessageDTO.Response.MessageWithAuthor> messageHistory = messageService.getMessageHistory(chatId, untilDT, depth);
            PrintWriter writer = resp.getWriter();
            writer.write(mapper.writeValueAsString(messageHistory));
        }
        catch (DateTimeParseException | NumberFormatException e){
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid param formatting");
        }
    }

}
