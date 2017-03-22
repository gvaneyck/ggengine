package com.gvaneyck.ggengine.server.dto.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ServerMessageDto {
    private final ServerCommand cmd = ServerCommand.MESSAGE;
    private String type;
    private String message;
    private long time;
}
