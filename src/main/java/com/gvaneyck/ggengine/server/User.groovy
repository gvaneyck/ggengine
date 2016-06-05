package com.gvaneyck.ggengine.server

import com.gvaneyck.ggengine.server.rooms.Room
import groovy.json.JsonBuilder
import org.java_websocket.WebSocket

public class User {

    String name
    List<Room> rooms = Collections.synchronizedList([])
    GameInstance gameInstance
    WebSocket conn

    public User(WebSocket conn) {
        this.conn = conn
    }

    public void setConn(WebSocket conn) {
        // TODO: Notify old socket
        this.conn = conn
    }

    public void sendMsg(String type, String room, Message msg) {
        send([cmd: 'chat', type: type, room: room, time: msg.time, message: msg.message, from: msg.from])
    }

    public void send(Map data) {
        synchronized (conn) {
            if (conn.isOpen()) {
                conn.send(new JsonBuilder(data).toString())
            }
        }
    }
}
