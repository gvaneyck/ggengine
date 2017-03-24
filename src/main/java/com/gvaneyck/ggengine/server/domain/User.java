package com.gvaneyck.ggengine.server.domain;

import com.gvaneyck.ggengine.server.GameServer;
import com.gvaneyck.ggengine.server.dto.server.ServerChatDto;
import com.gvaneyck.ggengine.server.util.JSON;
import lombok.Getter;
import lombok.Setter;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class User {

    private WebSocket conn;
    private boolean guest;
    private String name;
    private List<LobbyRoom> lobbyRooms;
    private List<GameRoom> gameRooms;
    private GameServer gameServer;

    public User(WebSocket conn) {
        this.conn = conn;
        this.lobbyRooms = Collections.synchronizedList(new ArrayList<>());
        this.gameRooms = Collections.synchronizedList(new ArrayList<>());
    }

    public void sendMessage(String type, String room, Message message) {
        send(new ServerChatDto(type, room, message.getFrom(), message.getMessage(), message.getTime()));
    }

    public void send(Object data) {
        synchronized (conn) {
            if (conn.isOpen()) {
                conn.send(JSON.writeValueAsString(data));
            }
        }
    }
}
