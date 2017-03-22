package com.gvaneyck.ggengine.server.services;

import com.gvaneyck.ggengine.server.GGException;
import com.gvaneyck.ggengine.server.GameServer;
import com.gvaneyck.ggengine.server.domain.GameRoom;
import com.gvaneyck.ggengine.server.domain.LobbyRoom;
import com.gvaneyck.ggengine.server.domain.User;
import com.gvaneyck.ggengine.server.dto.server.ServerLobbyJoinDto;
import com.gvaneyck.ggengine.server.dto.server.ServerLobbyLeaveDto;

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
        if (lobbies.containsKey(name)) {
            throw new GGException("Lobby already exists");
        }

        LobbyRoom lobbyRoom = new LobbyRoom(name, password);
        lobbies.put(lobbyRoom.getName(), lobbyRoom);
        joinLobby(user, name, password);
    }

    public void joinGeneralLobby(User user) {
        joinLobby(user, GENERAL, null);
    }

    public void joinLobby(User user, String name, String password) {
        // TODO: Send message history
        // TODO: Check password
        if (!lobbies.containsKey(name)) {
            throw new GGException("Lobby does not exist");
        }

        LobbyRoom room = lobbies.get(name);
        if (room.getUsers().contains(user)) {
            throw new GGException("User is already in lobby");
        }

        room.getUsers().add(user);
        user.getLobbyRooms().add(room);

        ServerLobbyJoinDto dto = new ServerLobbyJoinDto(room.getName(), user.getName());
        for (User u : room.getUsers()) {
            u.send(dto);
        }
    }

    public void leaveLobby(User user, String name) {
        if (!lobbies.containsKey(name)) {
            throw new GGException("Lobby does not exist");
        }

        LobbyRoom room = lobbies.get(name);
        if (!room.getUsers().contains(user)) {
            throw new GGException("User is not in lobby");
        }

        room.getUsers().remove(user);
        user.getLobbyRooms().remove(room);

        ServerLobbyLeaveDto dto = new ServerLobbyLeaveDto(room.getName(), user.getName());
        user.send(dto);
        for (User u : room.getUsers()) {
            u.send(dto);
        }

        if (room.getUsers().isEmpty() && !room.isPreserved()) {
            lobbies.remove(room.getName());
        }
    }

    public Collection<GameRoom> getGames() {
        return games.values();
    }

    public void createGame(User user, String name, String password, String game, int minSize, int maxSize) {
        if (games.containsKey(name)) {
            throw new GGException("Game already exists");
        }

        GameRoom gameRoom = new GameRoom(name, password, game, minSize, maxSize);
        games.put(gameRoom.getName(), gameRoom);
        joinGameAsPlayer(user, name, password);
    }

    public void joinGameAsPlayer(User user, String name, String password) {
        // TODO: Send game state if ongoing
        if (!games.containsKey(name)) {
            throw new GGException("Game room does not exist");
        }

        GameRoom room = games.get(name);
        if (room.getPlayers().contains(user)) {
            throw new GGException("User is already a player");
        }

        room.getSpectators().remove(user);
        room.getPlayers().add(user);
    }

    public void joinGameAsSpectator(User user, String name, String password) {
        // TODO: Send game state if ongoing
        if (!games.containsKey(name)) {
            throw new GGException("Game room does not exist");
        }

        GameRoom room = games.get(name);
        if (room.getSpectators().contains(user)) {
            throw new GGException("User is already a spectator");
        }

        room.getPlayers().remove(user);
        room.getSpectators().add(user);
    }

    public void leaveGame(User user, String name) {
        if (!games.containsKey(name)) {
            throw new GGException("Game room does not exist");
        }

        GameRoom room = games.get(name);
        if (!room.getPlayers().contains(user) && !room.getSpectators().contains(user)) {
            throw new GGException("User not in game");
        }

        room.getPlayers().remove(user);
        room.getSpectators().remove(user);

        if (room.getPlayers().isEmpty()) {
            games.remove(room.getName());
            // TODO: Preserve game and stop server
        }
    }

    public void startGame(User user, String name) {
        GameRoom room = games.get(name);
        if (room == null) {
            throw new GGException("Room does not exist");
        }

        if (room.getGameServer() != null) {
            throw new GGException("Game has already started");
        }

        if (room.getMinSize() > room.getPlayers().size() || room.getMaxSize() < room.getPlayers().size()) {
            throw new GGException("Game does not have the right number of players");
        }

        GameServer gameServer = new GameServer(room);
        room.setGameServer(gameServer);
        for (User u : room.getPlayers()) {
            u.setGameServer(gameServer);
        }
    }
}
