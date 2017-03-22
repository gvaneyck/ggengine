package com.gvaneyck.ggengine.server.dto.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ServerChatDto {
    private final ServerCommand cmd = ServerCommand.CHAT;
    private String type;
    private String room;
    private String from;
    private String message;
    private long time;
}
