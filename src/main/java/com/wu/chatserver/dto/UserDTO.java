package com.wu.chatserver.dto;

import com.wu.chatserver.domain.UsersChatSession;
import lombok.*;

import java.time.LocalDateTime;

public enum UserDTO {
    ;
    private interface Id{Long getId(); }
    private interface Name{String getUserName(); }
    private interface Password{String getPassword(); }
    private interface Email{String getEmail(); }
    private interface OnlineStatus{
        LocalDateTime getStartedAt();
        LocalDateTime getEndedAt();
        UsersChatSession.OnlineStatus getOnlineStatus();
    }
    private interface Token{
        String getToken();
    }

    public enum Request{
        ;
        @Getter
        @Setter
        @NoArgsConstructor
        public static class Registration implements Name, Password, Email{
            String userName;
            String password;
            String email;
        };

        @Getter
        @Setter
        @NoArgsConstructor
        public static class Login implements Name, Password{
            String userName;
            String password;
        }
    }

    public enum Response{
        ;
        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        public static class UserOnlineStatus implements Id, Name, OnlineStatus{
            private Long id;
            private String userName;
            private LocalDateTime startedAt;
            private LocalDateTime endedAt;
            private UsersChatSession.OnlineStatus onlineStatus;
        }

        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        public static class UserInfo implements Id, Name{
            private Long id;
            private String userName;
        }

        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        public static class UserLogin implements Id, Name, Token{
            private Long id;
            private String userName;
            private String token;
        }
    }
}
