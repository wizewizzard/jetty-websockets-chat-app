package com.wu.chatserver.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wu.chatserver.dto.UserDTO;
import com.wu.chatserver.exception.AuthenticationException;
import com.wu.chatserver.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class LoginServletTest {

    private LoginServlet loginServlet;
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
        loginServlet = new LoginServlet();
        loginServlet.setMapper(mapper);
        loginServlet.setUserService(userService);
    }

    @Test
    void successfulLoginShouldReturnToken() throws IOException, ServletException {
        String json = "{\"userName\":\"userName\", \"password\":\"password\"}";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        Mockito.when(response.getWriter()).thenReturn(printWriter);
        Mockito.when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        UserDTO.Response.UserLogin loginRespMock = new UserDTO.Response.UserLogin(1L, "userName", "tokenBLAH");
        Mockito.when(userService.loginUser(Mockito.any(UserDTO.Request.Login.class))).thenReturn(loginRespMock);

        loginServlet.doPost(request, response);

        printWriter.flush();
        assertThat(stringWriter.toString()).contains("tokenBLAH");
    }

    @Test
    void notSuccessfulLoginShouldReturnBadRequest() throws IOException, ServletException {
        String json = "{\"userName\":\"userName\", \"password\":\"password\"}";
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        Mockito.when(response.getWriter()).thenReturn(printWriter);
        Mockito.when(request.getReader()).thenReturn(new BufferedReader(new StringReader(json)));
        Mockito.when(userService.loginUser(Mockito.any(UserDTO.Request.Login.class))).thenThrow(new AuthenticationException("Login error"));

        loginServlet.doPost(request, response);
        printWriter.flush();

        Mockito.verify(response, Mockito.times(1)).sendError(HttpServletResponse.SC_BAD_REQUEST, "Login error");
    }
}