package com.gvaneyck.ggengine.server.dto.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ServerLoginDto {
    private final ServerCommand cmd = ServerCommand.LOGIN;
    private boolean success;
    private String name;
}
