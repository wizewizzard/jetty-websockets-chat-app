package com.wu.chatserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public enum ChatRoomDTO {
    ;
    private interface Id{ long getId(); }
    private interface Name{ String getChatName(); }

    public enum Request{
        ;
        @Getter
        @Setter
        @NoArgsConstructor
        public static class Delete implements Id{
            private long id;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        public static class Create implements Name{
            private String chatName;
        }
    }
}
