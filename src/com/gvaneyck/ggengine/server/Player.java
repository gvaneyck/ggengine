package com.gvaneyck.ggengine.server;

import com.gvaneyck.ggengine.GameManager;
import org.java_websocket.WebSocket;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public String name;

    public List<Lobby> lobbies = new ArrayList<Lobby>();

    public int player;
    public GameManager game;

    public WebSocket conn;

    public Player(WebSocket conn) {
        this.conn = conn;
    }

    public void sendMsg(String from, String to, String msg) {
        send("msg," + from + "," + to + "," + msg);
    }

    public void send(String msg) {
        synchronized (conn) {
            conn.send(msg);
        }
    }
}
