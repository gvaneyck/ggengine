package com.gvaneyck.ggengine.server.commands;

import com.gvaneyck.ggengine.server.domain.User;
import com.gvaneyck.ggengine.server.dto.client.ClientCommand;
import com.gvaneyck.ggengine.server.dto.client.ClientRoomDto;
import com.gvaneyck.ggengine.server.domain.GameRoom;
import com.gvaneyck.ggengine.server.services.RoomService;
import com.gvaneyck.ggengine.server.util.JSON;
import lombok.Setter;

public class RoomCommands {

    @Setter private RoomService roomService;

    @Command(ClientCommand.LOBBY_CREATE)
    public void createLobby(User user, LeaveRoomDto cmd) {
        roomService.leaveLobby(user, cmd.getName());
    }

    @Command(ClientCommand.LOBBY_JOIN)
    public void joinLobby(User user, LeaveRoomDto cmd) {
        ClientRoomDto cmd = JSON.convertValue(args, ClientRoomDto.class);
        String name = cmd.getName();
        String password = cmd.getPassword();
        String game = cmd.getGame();

        if (!rooms.get("game").containsKey(name)) {
            rooms.get("game").put(name, new GameRoom(name, game, password, 2, 2));
//            sendToAll([cmd: 'roomCreate', type: 'game', name: cmd.name])
        }

        GameRoom room = (GameRoom)rooms.get("game").get(name);
        if (!room.isPassword(password)) {
            return;
        }

        if (room.isFull()) {
            return;
        }

        room.join(user);
    }

    @Command(ClientCommand.LOBBY_LEAVE)
    public void leaveLobby(User user, LeaveRoomDto cmd) {
        roomService.leaveLobby(user, cmd.getName());
    }
}
