package com.gvaneyck.ggengine.server

import groovy.json.JsonBuilder
import org.java_websocket.WebSocket

import java.text.SimpleDateFormat

public class Player {
    def name
    def lobbies = []
    def id
    WebSocket conn

    public Player(WebSocket conn) {
        this.conn = conn
    }

    public void sendMsg(String from, String to, String msg) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.setTimeZone(TimeZone.getTimeZone('UTC'))
        send([cmd: 'chat', from: from, to: to, msg: msg, time: sdf.format(new Date())])
    }

    public void send(Map data) {
        synchronized (conn) {
            conn.send(new JsonBuilder(data).toString())
        }
    }
}
