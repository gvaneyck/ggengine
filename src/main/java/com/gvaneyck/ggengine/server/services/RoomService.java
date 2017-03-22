package com.gvaneyck.ggengine.server.services;

import com.gvaneyck.ggengine.server.User;
import com.gvaneyck.ggengine.server.rooms.GameRoom;
import com.gvaneyck.ggengine.server.rooms.LobbyRoom;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class RoomService {

    public static final String GENERAL = "General";

    private Map<String, LobbyRoom> lobbies = new LinkedHashMap<>();
    private Map<String, GameRoom> games = new LinkedHashMap<>();

    public RoomService() {
        LobbyRoom general = new LobbyRoom(GENERAL, null, true);
        lobbies.put(general.getName(), general);
    }

    public void createLobby(User user, String name, String password) {
        if (!lobbies.containsKey(name)) {
            LobbyRoom lobbyRoom = new LobbyRoom(name, password);
            lobbies.put(lobbyRoom.getName(), lobbyRoom);
        }
        joinLobby(user, name, password);
    }

    public LobbyRoom getLobby(String name) {
        return lobbies.get(name);
    }

    public void joinGeneralLobby(User user) {
        joinLobby(user, GENERAL, null);
    }

    public void joinLobby(User user, String name, String password) {
//        if (!users.contains(user)) {
//            users.add(user)
//            user.rooms.add(this)
//
//            users.each {
//                if (it != user) {
//                    it.send([cmd: 'roomJoin', type: type, name: name, member: user.name])
//                }
//            }
//            user.send([cmd: 'roomJoin', type: type, name: name, members: users.name, messages: messages.takeRight(10)])
//        }
    }

    public void leaveLobby(User user, String name) {
//        if (users.contains(user)) {
//            users.remove(user)
//            user.rooms.remove(this)
//
//            users.each {
//                it.send([cmd: 'roomLeave', type: type, name: name, member: user.name])
//            }
//        }
    }

    public Collection<GameRoom> getGames() {
        return games.values();
    }

    public void createGame(User user, String name, String password, String game, int minSize, int maxSize) {
        if (!games.containsKey(name)) {
            GameRoom gameRoom = new GameRoom(name, password, game, minSize, maxSize);
            games.put(gameRoom.getName(), gameRoom);
        }
    }

    public void joinGameAsPlayer(User user, String name, String password) {

    }

    public void joinGameAsSpectator(User user, String name, String password) {

    }

    public void leaveGame(User user, String name) {
        // Consider case where in game
        // Consider preserving games that are fully abandoned
    }

    public void startGame(User user, String name) {

    }
}
