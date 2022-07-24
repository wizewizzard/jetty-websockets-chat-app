package com.wu.chatserver.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wu.chatserver.dto.UserDTO;
import com.wu.chatserver.exception.RegistrationException;
import com.wu.chatserver.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

class RegisterServletTest {
    private RegisterServlet registerServlet;
    private HttpServletRequest request;
    private HttpServletResponse response;

    private ObjectMapper mapper;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = Mockito.mock(UserService.class);
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        mapper = new ObjectMapper();
        registerServlet = new RegisterServlet();
        registerServlet.setMapper(mapper);
        registerServlet.setUserService(userService);
    }

    @Test
    void registrationTest() throws IOException, ServletException {
        String json = "{\"userName\":\"userName\", \"password\":\"password\", \"email\":\"email\"}";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        Mockito.when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        Mockito.when(request.getContentType()).thenReturn("application/json");
        Mockito.when(response.getWriter()).thenReturn(printWriter);
        //Mockito.when(userService.registerUser(Mockito.any(UserDTO.Request.Registration.class))).thenReturn("stubToken");

        registerServlet.doPost(request, response);
        printWriter.flush();

        Mockito.verify(response, Mockito.times(1)).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void registrationFailureTest() throws IOException, ServletException {
        String json = "{\"userName\":\"userName\", \"password\":\"password\", \"email\":\"email\"}";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        Mockito.when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        Mockito.when(request.getContentType()).thenReturn("application/json");
        Mockito.when(response.getWriter()).thenReturn(printWriter);
        Mockito.doThrow(new RegistrationException("Error message")).when(userService).registerUser(Mockito.any(UserDTO.Request.Registration.class));

        registerServlet.doPost(request, response);
        printWriter.flush();
        Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Error message");
    }
}