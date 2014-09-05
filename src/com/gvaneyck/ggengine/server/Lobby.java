package com.gvaneyck.ggengine.server;

import java.util.ArrayList;
import java.util.List;

public class Lobby {
    public String game;

    public String name;
    public String password;

    public List<Player> players = new ArrayList<Player>();

    public int size = 2;

    public Lobby(String game, String name) {
        this(game, name, null);
    }

    public Lobby(String game, String name, String password) {
        this.name = name;
        this.password = password;
    }

    public void sendMsg(String from, String msg) {
        for (Player p : players) {
            p.sendMsg(from, name, msg);
        }
    }

    public void send(String msg) {

    }
}
