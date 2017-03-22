package com.gvaneyck.ggengine.server.domain;

import com.gvaneyck.ggengine.server.GameServer;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class GameRoom {

    String name;
    String password;
    List<User> users;
    List<User> spectators;
    List<Message> messages;

    String game;
    int minSize;
    int maxSize;

    GameServer gameServer;

    public GameRoom(String name, String password, String game, int minSize, int maxSize) {
        this.name = name;
        this.password = password;
        this.users = Collections.synchronizedList(new ArrayList<>());
        this.spectators = Collections.synchronizedList(new ArrayList<>());
        this.messages = Collections.synchronizedList(new ArrayList<>());

        this.game = game;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }
}
