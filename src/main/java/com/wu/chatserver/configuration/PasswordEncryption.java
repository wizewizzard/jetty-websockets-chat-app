package com.wu.chatserver.configuration;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class PasswordEncryption {
    @Produces
    @Dependent
    public PasswordEncryptor passwordEncryptor(){
        return new StrongPasswordEncryptor();
    }
}
