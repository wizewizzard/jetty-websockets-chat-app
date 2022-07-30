//
// ========================================================================
// Copyright (c) Webtide LLC and others.
//
// This program and the accompanying materials are made available under
// the terms of the Eclipse Public License 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0
//
// This Source Code may also be made available under the following
// Secondary Licenses when the conditions for such availability set
// forth in the Eclipse Public License, v. 2.0 are satisfied:
// the Apache License v2.0 which is available at
// https://www.apache.org/licenses/LICENSE-2.0
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package com.wu.chatserver;

import com.wu.chatserver.jwtauth.JwtAuthenticator;
import com.wu.chatserver.jwtauth.JwtManager;
import com.wu.chatserver.servlet.*;
import com.wu.chatserver.websocket.ChatSocket;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.cdi.CdiDecoratingListener;
import org.eclipse.jetty.cdi.CdiServletContainerInitializer;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.websocket.javax.server.config.JavaxWebSocketServletContainerInitializer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        try {
            new ChatServer().run();
        } catch (Throwable t) {
            System.out.println("Error occurred. " + t.getMessage());
            t.printStackTrace();
        }
    }

    public void run() throws Exception {
        String workingDir = System.getProperty("user.dir");
        System.out.println("Current working directory : " + workingDir);
        Properties properties = readProperties();

        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        URL resource = ChatServer.class.getClassLoader().getResource("application.properties");
        String replace = resource.toURI().toString().replace("/application.properties", "");
        context.setBaseResource(Resource.newResource(replace));
        // Enable Weld + CDI
        context.setInitParameter(CdiServletContainerInitializer.CDI_INTEGRATION_ATTRIBUTE, CdiDecoratingListener.MODE);
        context.addServletContainerInitializer(new CdiServletContainerInitializer());
        context.addServletContainerInitializer(new org.jboss.weld.environment.servlet.EnhancedListener());

        context.setSecurityHandler(createSecurityHandler(properties));
        context.addServlet(new ServletHolder(LoginServlet.class), "/login");
        context.addServlet(new ServletHolder(RegisterServlet.class), "/register");
        context.addServlet(new ServletHolder(TokenVerifyServlet.class), "/api/auth/verify");
        context.setErrorHandler(new ErrorHandler());
        server.setHandler(context);
        server.start();
        server.join();
    }

    private ConstraintSecurityHandler createSecurityHandler(Properties properties) {
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();

        Constraint constraint = new Constraint();
        constraint.setName("JWT_AUTH");
        constraint.setRoles(new String[]{"user"});
        constraint.setAuthenticate(true);

        ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/chat");

        Objects.requireNonNull(properties.getProperty("jwt.issuer"));
        Objects.requireNonNull(properties.getProperty("jwt.secret"));

        security.addConstraintMapping(cm);
        JwtAuthenticator jwtAuthenticator = new JwtAuthenticator();
        jwtAuthenticator.setJwtManager(new JwtManager(properties.getProperty("jwt.issuer"),
                Base64.getEncoder().encode(properties.getProperty("jwt.secret")
                        .getBytes(StandardCharsets.UTF_8))));
        security.setAuthenticator(jwtAuthenticator);
        log.info("Security handler created: " + security);
        return security;
    }

    private Properties readProperties() {
        Properties properties = new Properties();
        final InputStream stream = ChatServer.class.getClassLoader()
                .getResourceAsStream("application.properties");
        if (stream == null) {
            throw new RuntimeException("No properties file");
        }
        try {
            properties.load(stream);
        } catch (final IOException e) {
            throw new RuntimeException("Configuration could not be loaded!");
        }
        return properties;
    }


}
