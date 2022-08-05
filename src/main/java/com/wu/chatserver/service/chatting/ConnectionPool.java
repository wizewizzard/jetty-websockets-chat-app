package com.wu.chatserver.service.chatting;

import com.wu.chatserver.domain.User;
import com.wu.chatserver.domain.UsersChatSession;
import com.wu.chatserver.exception.ChatException;
import com.wu.chatserver.service.UserService;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.weld.context.activator.ActivateRequestContext;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Holds connections grouped by users that use them
 */
@ApplicationScoped
@Slf4j
@NoArgsConstructor
public class ConnectionPool {
    private final Map<User, List<RoomConnection>> userOpenedConnections = new ConcurrentHashMap<>();
    private UserService userService;

    @Inject
    public ConnectionPool(UserService userService) {
        this.userService = userService;
    }

    public List<RoomConnection> getUserConnections(User user) {
        return userOpenedConnections.get(user);
    }

    @ActivateRequestContext
    public RoomConnection establishConnection(ConnectionCredentials connectionCredentials) {
        User user = userService.getUserByUserName(connectionCredentials.getUserName()).orElseThrow(() -> new ChatException("No user found"));
        RoomConnection roomConnection = new RoomConnection(user);
        if (userOpenedConnections.get(user) == null) {
            //First connection user opened. Online status must be set
            userService.userSetOnlineStatus(user.getId(), UsersChatSession.OnlineStatus.ONLINE);
        }
        userOpenedConnections.merge(user,
                Collections.synchronizedList(new LinkedList<>(List.of(roomConnection))),
                (l1, l2) -> Collections.synchronizedList(Stream.concat(l1.stream(), l2.stream()).collect(Collectors.toList())));
        return roomConnection;
    }

    @ActivateRequestContext
    public void closeConnection(RoomConnection roomConnection) {
        User user = roomConnection.getUser();
        log.debug("Disconnecting user {}", user.getUserName());
        List<RoomConnection> roomConnections = userOpenedConnections.get(user);
        roomConnection.closeConnection();
        roomConnections.remove(roomConnection);
        if (roomConnections.isEmpty()) {
            userOpenedConnections.remove(user);
            //Last user's connection. Offline status must be set
            userService.userSetOnlineStatus(user.getId(), UsersChatSession.OnlineStatus.OFFLINE);
        }
        log.debug("Connection for user {} was removed", user.getUserName());
    }
}
