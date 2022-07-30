package com.wu.chatserver.jwtauth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.DefaultUserIdentity;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.UserAuthentication;
import org.eclipse.jetty.security.authentication.DeferredAuthentication;
import org.eclipse.jetty.server.Authentication;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Slf4j
public class JwtAuthenticator implements Authenticator {

    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String AUTH_HEADER_VALUE_PREFIX = "Bearer ";
    private static final String TOKEN_COOKIE_NAME = "token ";

    private static final String ENV_JWT_SECRET = "JWT_SECRET";
    private static final String ENV_JWT_ISSUER = "JWT_ISSUER";

    @Getter
    @Setter
    @Inject
    private JwtManager jwtManager;

    @Override
    public void setConfiguration(AuthConfiguration configuration) {
        log.trace("setConfiguration called");
        if(jwtManager == null)
            throw new IllegalStateException("JWT Manager is not set");
    }

    @Override
    public String getAuthMethod() {
        log.trace("getAuthMethod called");
        return "JWT";
    }

    @Override
    public void prepareRequest(ServletRequest request) {
        log.trace("prepareRequest called");
        //noop
    }

    @Override
    public Authentication validateRequest(ServletRequest request,
                                          ServletResponse response,
                                          boolean mandatory) throws ServerAuthException {
        log.trace("validateRequest called");
        if (!mandatory)
            return Authentication.UNAUTHENTICATED;

        HttpServletRequest httpReq = (HttpServletRequest) request;
        String token = getToken(httpReq);
        if(token != null){
            try{
                log.debug("Verifying token");
                Jws<Claims> claims = jwtManager.parse(token);
                Long userId = claims.getBody().get("userId", Long.class);
                String userName = claims.getBody().get("userName", String.class);
                if(userName != null && userId != null){
                    log.info("User authenticated: {}", userName);
                    return new UserAuthentication(getAuthMethod(),
                            new DefaultUserIdentity( null, new JwtPrincipal(userId, userName), new String[]{"user"}));
                }
            }
            catch (Exception exc){
                log.debug("JWT token validation failed.");
                log.debug("Exception fired.", exc);
            }
        }
        log.info("Returning UNAUTHENTICATED");
        return Authentication.UNAUTHENTICATED;
    }

    @Override
    public boolean secureResponse(ServletRequest request,
                                  ServletResponse response,
                                  boolean mandatory,
                                  Authentication.User validatedUser) throws ServerAuthException {
        log.trace("secureResponse called");
        return true;
    }

    private String getToken(HttpServletRequest request){
        String authorizationHeader = request.getHeader(AUTH_HEADER_NAME);
        if(authorizationHeader != null && authorizationHeader.startsWith(AUTH_HEADER_VALUE_PREFIX)){
            log.debug("Found the jwt token in a header");
            return authorizationHeader.substring(AUTH_HEADER_VALUE_PREFIX.length());
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null)
            for (Cookie cookie: cookies)
                if(cookie.getName().equals(TOKEN_COOKIE_NAME)){
                    log.debug("Found the jwt token in cookies");
                    return cookie.getValue();
                }
        log.debug("No token was found in a request");
        return null;
    }
}
