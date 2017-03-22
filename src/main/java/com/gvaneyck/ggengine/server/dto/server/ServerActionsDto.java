package com.gvaneyck.ggengine.server.dto.server;

import com.gvaneyck.ggengine.game.actions.ActionOption;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ServerActionsDto {
    private final ServerCommand cmd = ServerCommand.ACTIONS;
    private List<ActionOption> actionOptions;
}
