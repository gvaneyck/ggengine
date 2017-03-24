package com.gvaneyck.ggengine.server.commands;

import com.gvaneyck.ggengine.server.domain.User;
import com.gvaneyck.ggengine.server.dto.client.ClientCommand;
import com.gvaneyck.ggengine.server.dto.client.ClientMessageDto;
import com.gvaneyck.ggengine.server.services.MessageService;
import lombok.Setter;

public class MessageCommands {

    @Setter private MessageService messageService;

    @Command(ClientCommand.SEND_MESSAGE)
    public void sendMessage(User user, ClientMessageDto cmd) {
        messageService.handleClientMessage(user, cmd.getType(), cmd.getTarget(), cmd.getMessage());
    }
}
