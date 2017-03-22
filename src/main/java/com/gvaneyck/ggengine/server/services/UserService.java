package com.gvaneyck.ggengine.server.services;

import com.gvaneyck.ggengine.server.GGException;
import com.gvaneyck.ggengine.server.domain.GameRoom;
import com.gvaneyck.ggengine.server.domain.LobbyRoom;
import com.gvaneyck.ggengine.server.domain.User;
import org.java_websocket.WebSocket;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserService {

    private RoomService roomService;

    private Map<WebSocket, User> socketToUser = Collections.synchronizedMap(new LinkedHashMap<>());
    private Map<String, User> nameToUser = Collections.synchronizedMap(new LinkedHashMap<>());
    // TODO: maps to list of users for one account logged in multiple times
    // Make sure logging off only removes one connection
    // Make sure room state is copied when logging on, maybe more state?

    public UserService(RoomService roomService) {
        this.roomService = roomService;
    }

    public User getUser(WebSocket webSocket) {
        return socketToUser.get(webSocket);
    }

    public void createGuest(WebSocket webSocket) {
        User user = new User(webSocket);
        socketToUser.put(webSocket, user);
    }

    public void login(User user, String name, String password) {
        if (nameToUser.containsKey("name")) {
            throw new GGException("User is already logged in");
        }

        user.setName(name);
        nameToUser.put(name, user);
    }

    public void logout(WebSocket webSocket) {
        User user = socketToUser.remove(webSocket);
        if (user != null) {
            nameToUser.remove(user.getName());

            for (LobbyRoom lobbyRoom : user.getLobbyRooms()) {
                roomService.leaveLobby(user, lobbyRoom.getName());
            }

            for (GameRoom gameRoom : user.getGameRooms()) {
                roomService.leaveGame(user, gameRoom.getName());
            }
        }
        webSocket.close();
    }
}
