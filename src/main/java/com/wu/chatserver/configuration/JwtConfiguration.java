package com.wu.chatserver.configuration;

import com.wu.chatserver.jwtauth.JwtManager;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

@Slf4j
public class JwtConfiguration {
    @Produces
    @ApplicationScoped
    public JwtManager jwtManager(Properties properties) {
        return new JwtManager(properties.getProperty("jwt.issuer"),
                Base64.getEncoder().encode(properties.getProperty("jwt.secret")
                        .getBytes(StandardCharsets.UTF_8)));
    }

}
