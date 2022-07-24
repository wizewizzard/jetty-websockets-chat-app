package com.wu.chatserver.jwtauth;

import lombok.Getter;

import java.security.Principal;
import java.util.Objects;

public class JwtPrincipal implements Principal {

    @Getter
    private Long userId;
    private String userName;

    public JwtPrincipal(Long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public String getName() {
        return userName;
    }

    public String toString() {
        return getName();
    }

    public boolean equals(final Object o) {
        if (o == null) {
            return false;
        } else if (!(o instanceof JwtPrincipal)) {
            return false;
        } else {
            return getName().equals(((JwtPrincipal) o).getName());
        }
    }

    public int hashCode() {
        return Objects.hash(getName());
    }
}
