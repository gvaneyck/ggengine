package com.gvaneyck.ggengine.server.commands;

import com.gvaneyck.ggengine.server.domain.User;
import com.gvaneyck.ggengine.server.dto.client.ClientCommand;
import com.gvaneyck.ggengine.server.dto.client.ClientNameDto;
import com.gvaneyck.ggengine.server.dto.client.ClientRoomDto;
import com.gvaneyck.ggengine.server.services.RoomService;
import lombok.Setter;

public class LobbyCommands {

    @Setter private RoomService roomService;

    @Command(ClientCommand.LOBBY_CREATE)
    public void createLobby(User user, ClientRoomDto cmd) {
        roomService.createLobby(user, cmd.getName(), cmd.getPassword());
    }

    @Command(ClientCommand.LOBBY_JOIN)
    public void joinLobby(User user, ClientRoomDto cmd) {
        roomService.joinLobby(user, cmd.getName(), cmd.getPassword());
    }

    @Command(ClientCommand.LOBBY_LEAVE)
    public void leaveLobby(User user, ClientNameDto cmd) {
        roomService.leaveLobby(user, cmd.getName());
    }
}
