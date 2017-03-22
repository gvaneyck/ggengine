package com.gvaneyck.ggengine.server.dto.server;

public enum ServerCommand {
    ACTIONS("actions"),
    CHAT("chat"),
    END("end"),
    GAME_LIST("gameList"),
    GS("gs"),
    LOGIN("login"),
    MESSAGE("message");

    private final String command;

    ServerCommand(String command) {
        this.command = command;
    }

    public String toString() {
        return command;
    }
}
