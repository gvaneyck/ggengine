package com.gvaneyck.ggengine.server.domain;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class LobbyRoom {

    private String name;
    private String password;
    private boolean preserved;
    private List<User> users;
    private List<Message> messages;

    public LobbyRoom(String name, String password) {
        this(name, password, false);
    }

    public LobbyRoom(String name, String password, boolean preserved) {
        this.name = name;
        this.password = password;
        this.preserved = preserved;
        this.users = Collections.synchronizedList(new ArrayList<>());
        this.messages = Collections.synchronizedList(new ArrayList<>());
    }
}
