package com.wu.chatserver.servlet;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wu.chatserver.dto.TokenDTO;
import com.wu.chatserver.dto.UserDTO;
import com.wu.chatserver.exception.AuthenticationException;
import com.wu.chatserver.exception.RegistrationException;
import com.wu.chatserver.service.UserService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@NoArgsConstructor
public class RegisterServlet extends HttpServlet {
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
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.info("Registering user");
        try{
            UserDTO.Request.Registration registration = mapper.readValue(req.getReader(), UserDTO.Request.Registration.class);
            if(!validate(registration)){
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "User name, email and password are mandatory");
                return;
            }
            userService.registerUser(registration);
            resp.setStatus(HttpServletResponse.SC_CREATED);
        }
        catch (DatabindException exception){
            log.debug("Wrong data format");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Wrong data format");
        }
        catch (RegistrationException exception){
            log.debug("Credentials already is use");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }
    }

    private boolean validate(UserDTO.Request.Registration registration){
        return !(registration.getUserName() == null || registration.getUserName().isBlank()
                || registration.getPassword() == null | registration.getPassword().isBlank()
                || registration.getEmail() == null | registration.getEmail().isBlank());
    }
}
