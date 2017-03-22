package com.gvaneyck.ggengine.server.dto.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ServerLobbyLeaveDto {
    private final ServerCommand cmd = ServerCommand.LOBBY_LEAVE;
    private String name;
    private String user;
}
