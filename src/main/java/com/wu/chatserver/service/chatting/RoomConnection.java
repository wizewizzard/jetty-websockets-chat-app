package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * This interface supposed to know how to contact with specific user.
 */
//TODO: think about naming of this class
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoomConnection {
    @EqualsAndHashCode.Include
    @Getter
    private final User user;
    @Getter
    @Setter
    private RoomMembership membership;

    public RoomConnection(User user) {
        this.user = user;
    }

    public RoomConnection(User user,
                          RoomMembership membership
                          ) {
        this.user = user;
        this.membership = membership;
    }

    public void closeConnection(){
        if(membership != null){
            membership.utilize();
        }
    }
}
