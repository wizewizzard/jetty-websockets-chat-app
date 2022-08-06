package com.wu.chatserver.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class DtoMapping {
    @Produces
    @Dependent
    @Default
    public ObjectMapper objectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        //mapper.findAndRegisterModules();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
