package com.gvaneyck.ggengine.server.dto.server;

public enum ServerCommand {
    ACTIONS("actions"),
    CHAT("chat"),
    GAME_CREATE("gameCreate"),
    GAME_JOIN("gameJoin"),
    GAME_LEAVE("gameLeave"),
    GAME_LIST("gameList"),
    GAME_STATE("gameState"),
    LOBBY_CREATE("lobbyCreate"),
    LOBBY_JOIN("lobbyJoin"),
    LOBBY_LEAVE("lobbyLeave"),
    LOBBY_LIST("lobbyList"),
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
