package com.gvaneyck.ggengine.server.services;

import com.gvaneyck.ggengine.server.domain.GameRoom;
import com.gvaneyck.ggengine.server.domain.LobbyRoom;
import com.gvaneyck.ggengine.server.domain.User;

public class MessageService {

    public void sendMessageToUser(User recipient, String type, String from, String message) {

    }

    public void sendMessageToLobby(LobbyRoom recipient, String type, String from, String message) {

    }

    public void sendMessageToGame(GameRoom recipient, String type, String from, String message) {

    }

    public void handleClientMessage(User sender, String type, String to, String message) {

    }
}
