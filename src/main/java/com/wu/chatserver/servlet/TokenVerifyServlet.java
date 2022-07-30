package com.wu.chatserver.servlet;

import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wu.chatserver.dto.TokenDTO;
import com.wu.chatserver.dto.UserDTO;
import com.wu.chatserver.exception.AuthenticationException;
import com.wu.chatserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.http.MimeTypes;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public class TokenVerifyServlet extends HttpServlet {
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
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        log.debug("Verifying token");
        try {
            String token = req.getParameter("token");
            TokenDTO tokenDTO = new TokenDTO(token);
            if (!validate(tokenDTO)) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid token");
                return;
            }
            UserDTO.Response.UserInfo userInfo = userService.verifyToken(tokenDTO);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());
            resp.getWriter().println(mapper.writeValueAsString(userInfo));
        }catch (AuthenticationException exception) {
            log.debug("Invalid token to verify");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid token");
        }
        catch (DatabindException exception) {
            log.debug("Wrong data format");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad token");
        }
    }

    private boolean validate(TokenDTO tokenDTO) {
        return !(tokenDTO == null
                || tokenDTO.getToken() == null
                || tokenDTO.getToken().isBlank()
        );
    }
}
