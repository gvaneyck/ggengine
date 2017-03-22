package com.gvaneyck.ggengine.server.commands;

import com.gvaneyck.ggengine.game.actions.ActionRef;
import com.gvaneyck.ggengine.server.GameServer;
import com.gvaneyck.ggengine.server.User;
import com.gvaneyck.ggengine.server.dto.client.ClientActionDto;
import com.gvaneyck.ggengine.server.dto.client.ClientCommand;
import com.gvaneyck.ggengine.server.dto.client.ClientGameDto;
import com.gvaneyck.ggengine.server.dto.client.ClientNameDto;
import com.gvaneyck.ggengine.server.dto.client.ClientRoomDto;
import com.gvaneyck.ggengine.server.rooms.GameRoom;
import com.gvaneyck.ggengine.server.services.RoomService;
import com.gvaneyck.ggengine.server.util.JSON;
import lombok.Setter;

import java.util.List;

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
    }

    @Command(ClientCommand.GAME_START)
    public void startGame(User user, ClientNameDto cmd) {
        String name = args.get("name").toString();
        if (name == null) {
            return;
        }

        GameRoom room = (GameRoom)rooms.get("game").get(name);
        if (room == null) {
            return;
        }

        if (room.canStart()) {
            GameServer gameServer = new GameServer(room);
            room.setGameServer(gameServer);
            for (User u : room.getUsers()) {
                u.setGameServer(gameServer);
            }
        }
    }

    @Command(ClientCommand.PLAY_ACTION)
    public void playAction(User user, ClientActionDto clientActionDto) {
        ClientActionDto cmd = JSON.convertValue(args, ClientActionDto.class);
        ActionRef actionRef = JSON.convertValue(args.get("actionRef"), ActionRef.class);
        List<Object> actionArgs = (List<Object>)args.get("args");
//        if (user.getGameServer().isDone()) {
//            user.setGameServer(null);
//        } else {
//            user.getGameServer().setChoice(user, actionRef, actionArgs);
//        }
    }
}
