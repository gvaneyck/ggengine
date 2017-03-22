package com.gvaneyck.ggengine.server.dto.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class ServerGsDto {
    private final ServerCommand cmd = ServerCommand.GS;
    private Map<String, Object> gs;
}
