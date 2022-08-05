package com.wu.chatserver.repository.util;

import com.wu.chatserver.domain.*;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TestData {
    @Getter
    private List<User> users;
    @Getter
    private List<ChatRoom> chatRooms;

    public TestData(){

        initUsers();
        initChatRooms();
        initMessages();
        initUserSessions();

        users = Collections.unmodifiableList(users);
        chatRooms = Collections.unmodifiableList(chatRooms);
    }

    private void initUsers(){
        users = new ArrayList<>();

        User jack = new User();
        jack.setUserName("Jack");
        jack.setEmail("jack@mail.com");
        jack.setPassword("jackpassword");
        users.add(jack);

        User julia = new User();
        julia.setUserName("Julia");
        julia.setEmail("julia@mail.com");
        julia.setPassword("juliapassword");
        users.add(julia);

        User harry = new User();
        harry.setUserName("Harry");
        harry.setEmail("harry@mail.com");
        harry.setPassword("harrypassword");
        users.add(harry);

        User denny = new User();
        denny.setUserName("Denny");
        denny.setEmail("denny@mail.com");
        denny.setPassword("dennypassword");
        users.add(denny);
    }
    private void initChatRooms(){
        Objects.requireNonNull(users);
        User julia = users.stream().filter(u -> u.getUserName().equals("Julia")).findFirst().orElseThrow();
        User harry = users.stream().filter(u -> u.getUserName().equals("Harry")).findFirst().orElseThrow();
        User jack = users.stream().filter(u -> u.getUserName().equals("Jack")).findFirst().orElseThrow();
        User denny = users.stream().filter(u -> u.getUserName().equals("Denny")).findFirst().orElseThrow();

        chatRooms = new ArrayList<>();

        ChatRoom chatRoom1 = new ChatRoom();
        chatRoom1.setCreatedBy(julia);
        chatRoom1.setName("Julia's chat");
        chatRoom1.addMember(julia);
        chatRoom1.addMember(jack);
        chatRooms.add(chatRoom1);

        ChatRoom chatRoom2 = new ChatRoom();
        chatRoom2.setName("Harry's chat #1");
        chatRoom2.setCreatedBy(harry);
        chatRoom2.addMember(harry);
        chatRooms.add(chatRoom2);

        ChatRoom chatRoom3 = new ChatRoom();
        chatRoom3.setName("Denny's chat");
        chatRoom3.setCreatedBy(harry);
        chatRoom3.addMember(harry);
        chatRooms.add(chatRoom3);

        ChatRoom chatRoom4 = new ChatRoom();
        chatRoom4.setName("Harry's chat #2");
        chatRoom4.setCreatedBy(harry);
        chatRoom4.addMember(harry);
        chatRoom4.addMember(julia);
        chatRoom4.addMember(jack);
        chatRooms.add(chatRoom4);
    }
    private void initMessages(){
        Objects.requireNonNull(users);
        Objects.requireNonNull(chatRooms);

        User julia = users.stream().filter(u -> u.getUserName().equals("Julia")).findFirst().orElseThrow();
        User jack = users.stream().filter(u -> u.getUserName().equals("Jack")).findFirst().orElseThrow();
        User harry = users.stream().filter(u -> u.getUserName().equals("Harry")).findFirst().orElseThrow();

        ChatRoom juliasChatRoom = chatRooms.stream().filter(u -> u.getName().equals("Julia's chat")).findFirst().orElseThrow();
        ChatRoom harrysSecondChatRoom = chatRooms.stream().filter(u -> u.getName().equals("Harry's chat #2")).findFirst().orElseThrow();

        Message messageByJack1 = new Message();
        messageByJack1.setBody("Hi julia");
        messageByJack1.setCreatedBy(jack);
        messageByJack1.setPublishedAt(LocalDateTime.parse("2022-07-18T10:00:00Z", DateTimeFormatter.ISO_DATE_TIME));
        juliasChatRoom.addMessage(messageByJack1);

        Message messageByJulia1 = new Message();
        messageByJulia1.setBody("Hello jack!");
        messageByJulia1.setCreatedBy(julia);
        messageByJulia1.setPublishedAt(LocalDateTime.parse("2022-07-18T11:00:00Z", DateTimeFormatter.ISO_DATE_TIME));
        juliasChatRoom.addMessage(messageByJulia1);

        Message messageByJack2 = new Message();
        messageByJack2.setBody("It is a nice weather today isn't it?");
        messageByJack2.setCreatedBy(jack);
        messageByJack2.setPublishedAt(LocalDateTime.parse("2022-07-18T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME));
        juliasChatRoom.addMessage(messageByJack2);

        Message messageByHarry1 = new Message();
        messageByHarry1.setBody("Lonely...");
        messageByHarry1.setCreatedBy(harry);
        messageByHarry1.setPublishedAt(LocalDateTime.parse("2022-07-18T13:00:00Z", DateTimeFormatter.ISO_DATE_TIME));
        harrysSecondChatRoom.addMessage(messageByHarry1);


    }
    private void initUserSessions(){
        Objects.requireNonNull(users);
        Objects.requireNonNull(chatRooms);

        User julia = users.stream().filter(u -> u.getUserName().equals("Julia")).findFirst().orElseThrow();
        User jack = users.stream().filter(u -> u.getUserName().equals("Jack")).findFirst().orElseThrow();
        User harry = users.stream().filter(u -> u.getUserName().equals("Harry")).findFirst().orElseThrow();

        ChatRoom juliasChatRoom = chatRooms.stream().filter(u -> u.getName().equals("Julia's chat")).findFirst().orElseThrow();
        ChatRoom harrysChatRoom = chatRooms.stream().filter(u -> u.getName().equals("Harry's chat #1")).findFirst().orElseThrow();

        Set<UsersChatSession> juliasChatRoomSessions = juliasChatRoom.getUsersChatSessions();
        Set<UsersChatSession> harrysChatRoomSessions = harrysChatRoom.getUsersChatSessions();

        UsersChatSession jackChatSession = juliasChatRoomSessions
                .stream()
                .filter(s -> s.getChatSessionId().getUser().equals(jack))
                .findFirst()
                .orElseThrow();
        jackChatSession.setStartedAt(LocalDateTime.parse("2022-07-18T09:00:00Z", DateTimeFormatter.ISO_DATE_TIME));
        jackChatSession.setEndedAt(null);

        UsersChatSession juliaChatSession = juliasChatRoomSessions
                .stream()
                .filter(s -> s.getChatSessionId().getUser().equals(julia))
                .findFirst()
                .orElseThrow();
        juliaChatSession.setStartedAt(LocalDateTime.parse("2022-07-18T09:00:00Z", DateTimeFormatter.ISO_DATE_TIME));
        juliaChatSession.setEndedAt(LocalDateTime.parse("2022-07-18T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME));

        UsersChatSession harryChatSession = harrysChatRoomSessions
                .stream()
                .filter(s -> s.getChatSessionId().getUser().equals(harry))
                .findFirst()
                .orElseThrow();
        harryChatSession.setStartedAt(LocalDateTime.parse("2022-07-18T09:00:00Z", DateTimeFormatter.ISO_DATE_TIME));
        harryChatSession.setEndedAt(LocalDateTime.parse("2022-07-18T12:00:00Z", DateTimeFormatter.ISO_DATE_TIME));
    }
}
