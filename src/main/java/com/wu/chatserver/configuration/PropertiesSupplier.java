package com.wu.chatserver.configuration;

import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@ApplicationScoped
@Slf4j
public class PropertiesSupplier {
    @Produces
    @Singleton
    public Properties properties() {
        Properties properties = new Properties();
        final InputStream stream = PropertiesSupplier.class
                .getResourceAsStream("/application.properties");
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
