package com.gvaneyck.ggengine.server.dto.client;

public enum ClientCommand {
    NONE("none"),
    GAME_CREATE("createGame"),
    GAME_JOIN("joinGame"),
    GAME_LEAVE("leaveGame"),
    GAME_START("startGame"),
    LOBBY_LEAVE("startGame"),
    LOBBY_JOIN("startGame"),
    LOBBY_CREATE("startGame"),
    PLAY_ACTION("playAction"),
    SEND_MSG("startGame"),
    USER_LOGIN("login"),
    USER_LOGOUT("logout");

    private final String command;

    ClientCommand(String command) {
        this.command = command;
    }

    public String toString() {
        return command;
    }
}
