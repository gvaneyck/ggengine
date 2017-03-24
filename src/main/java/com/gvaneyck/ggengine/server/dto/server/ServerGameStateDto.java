package com.gvaneyck.ggengine.server.dto.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ServerGameStateDto {
    private final ServerCommand cmd = ServerCommand.GAME_STATE;
    private Map<String, Object> gs;
}
