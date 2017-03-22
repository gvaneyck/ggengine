package com.gvaneyck.ggengine.server.commands;

import com.gvaneyck.ggengine.server.GGException;
import com.gvaneyck.ggengine.server.domain.User;
import com.gvaneyck.ggengine.server.dto.client.ClientActionDto;
import com.gvaneyck.ggengine.server.dto.client.ClientCommand;
import com.gvaneyck.ggengine.server.dto.client.ClientGameDto;
import com.gvaneyck.ggengine.server.dto.client.ClientNameDto;
import com.gvaneyck.ggengine.server.dto.client.ClientRoomDto;
import com.gvaneyck.ggengine.server.services.RoomService;
import lombok.Setter;

public class GameCommands {

    @Setter private RoomService roomService;

    @Command(ClientCommand.GAME_CREATE)
    public void createGame(User user, ClientGameDto cmd) {
        roomService.createGame(user, cmd.getName(), cmd.getPassword(), cmd.getGame(), 2, 2);
        roomService.joinGameAsPlayer(user, cmd.getName(), cmd.getPassword());
    }

    @Command(ClientCommand.GAME_JOIN)
    public void joinGame(User user, ClientRoomDto cmd) {
        roomService.joinGameAsPlayer(user, cmd.getName(), cmd.getPassword());
    }

    @Command(ClientCommand.GAME_LEAVE)
    public void leaveGame(User user, ClientNameDto cmd) {
        roomService.leaveGame(user, cmd.getName());
    }

    @Command(ClientCommand.GAME_START)
    public void startGame(User user, ClientNameDto cmd) {
        roomService.startGame(user, cmd.getName());
    }

    @Command(ClientCommand.PLAY_ACTION)
    public void playAction(User user, ClientActionDto clientActionDto) {
        if (user.getGameServer() == null) {
            throw new GGException("User is not in a game");
        }

        user.getGameServer().setChoice(user, clientActionDto.getActionRef(), clientActionDto.getArgs());
    }
}
