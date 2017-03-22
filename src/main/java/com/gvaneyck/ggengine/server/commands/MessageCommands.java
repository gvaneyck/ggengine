package com.gvaneyck.ggengine.server.commands;

import com.gvaneyck.ggengine.server.domain.Message;
import com.gvaneyck.ggengine.server.domain.User;
import com.gvaneyck.ggengine.server.dto.client.ClientCommand;
import com.gvaneyck.ggengine.server.dto.client.ClientMessageDto;
import com.gvaneyck.ggengine.server.services.RoomService;
import com.gvaneyck.ggengine.server.util.JSON;
import lombok.Setter;

public class MessageCommands {

    @Setter private RoomService roomService;

    @Command(ClientCommand.SEND_MSG)
    public void sendMsg(User user, LeaveRoomDto cmd) {
        ClientMessageDto cmd = JSON.convertValue(args, ClientMessageDto.class);
        String type = cmd.getType();
        String target = cmd.getTarget();
        String msg = cmd.getMsg();

        Room room = (rooms.containsKey(type) ? rooms.get(type).get(target) : null);
        if (room == null) {
            return;
        }

        room.send(new Message(msg, user.getName()));
    }
}
