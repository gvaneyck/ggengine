package com.gvaneyck.ggengine.server.dto.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ServerGameListDto {
    private final ServerCommand cmd = ServerCommand.LOGIN;
    private List<String> names;
}
