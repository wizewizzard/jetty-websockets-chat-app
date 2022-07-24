package com.wu.chatserver.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

public class DtoMapping {
    @Produces
    @Dependent
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }
}
