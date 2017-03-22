package com.gvaneyck.ggengine.server.services;

import com.gvaneyck.ggengine.server.User;
import org.java_websocket.WebSocket;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserService {

    private Map<WebSocket, User> socketToUser = Collections.synchronizedMap(new LinkedHashMap<>());
    private Map<String, User> nameToUser = Collections.synchronizedMap(new LinkedHashMap<>());
    // TODO: maps to list of users for one account logged in multiple times
    // Make sure logging off only removes one connection
    // Make sure room state is copied when logging on, maybe more state?

    public User getUser(WebSocket webSocket) {
        return socketToUser.get(webSocket);
    }

    public void createGuest(WebSocket webSocket) {
        User user = new User(webSocket);
        socketToUser.put(webSocket, user);
    }

    public void login(User user, String name, String password) {
        if (nameToUser.containsKey("name")) {
            throw new RuntimeException("User is already logged in");
        }

        user.setName(name);
        nameToUser.put(name, user);
    }

    public void logout(WebSocket webSocket) {
        User user = socketToUser.remove(webSocket);
        if (user != null) {
            nameToUser.remove(user.getName());
        }
        webSocket.close();

        // TODO: leave rooms
//        while (!user.getRooms().isEmpty()) {
//            Room room = user.getRooms().get(0);
//            room.leave(user);
//            if (room.getUsers().isEmpty() && !room.getType().equals("lobby")) {
//                rooms.get(room.getType()).remove(room.getName());
//            sendToAll([cmd: "roomDestroy", type: room.type, name: room.name])
//            }
//        }
    }
}
