package com.gvaneyck.ggengine.server.commands;

import com.gvaneyck.ggengine.server.User;
import com.gvaneyck.ggengine.server.dto.client.ClientCommand;
import com.gvaneyck.ggengine.server.dto.client.ClientLoginDto;
import com.gvaneyck.ggengine.server.dto.server.ServerGameListDto;
import com.gvaneyck.ggengine.server.dto.server.ServerLoginDto;
import com.gvaneyck.ggengine.server.rooms.GameRoom;
import com.gvaneyck.ggengine.server.services.GameService;
import com.gvaneyck.ggengine.server.services.RoomService;
import com.gvaneyck.ggengine.server.services.UserService;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LoginCommands {

    @Setter private GameService gameService;
    @Setter private RoomService roomService;
    @Setter private UserService userService;

    @Command(ClientCommand.USER_LOGIN)
    public void login(User user, ClientLoginDto cmd) {
        String name = cmd.getName();
        name = name.trim();
        name = name.replaceAll("[^a-zA-Z0-9 ]", "");

        userService.login(user, cmd.getName(), cmd.getPassword());
        user.send(new ServerLoginDto(true, name));

        List<String> rooms = roomService.getGames().stream().map(GameRoom::getName).collect(Collectors.toList());
        user.send(new ServerGameListDto(rooms));

        roomService.joinGeneralLobby(user);
        gameService.reconnect(user);
    }

    @Command(ClientCommand.USER_LOGOUT)
    public void logout(User user, Map<String, Object> cmd) {
        userService.logout(user.getConn());

    }
}
