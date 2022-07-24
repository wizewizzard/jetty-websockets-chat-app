package com.wu.chatserver.servlet;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wu.chatserver.dto.TokenDTO;
import com.wu.chatserver.dto.UserDTO;
import com.wu.chatserver.exception.AuthenticationException;
import com.wu.chatserver.jwtauth.JwtAuthenticator;
import com.wu.chatserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.security.ConstraintSecurityHandler;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class LoginServlet extends HttpServlet {
    private UserService userService;

    private ObjectMapper mapper;

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Inject
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("Login user");

        try{
            UserDTO.Request.Login login = mapper.readValue(req.getReader(), UserDTO.Request.Login.class);
            if(!validate(login))
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "userName and password are mandatory");
            String token = userService.loginUser(login);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().println(mapper.writeValueAsString(new TokenDTO(token)));
        }
        catch (DatabindException exception){
            log.debug("Wrong data format");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong data format");
        }
        catch (AuthenticationException exception){
            log.debug("Credentials are wrong");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }
    }

    private boolean validate(UserDTO.Request.Login login){
        return !(login.getUserName() == null || login.getUserName().isBlank()
                || login.getPassword() == null || login.getPassword().isBlank());
    }

}
