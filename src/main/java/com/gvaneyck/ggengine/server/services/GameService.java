package com.gvaneyck.ggengine.server.services;

import com.gvaneyck.ggengine.server.domain.User;

public class GameService {

    public void reconnect(User user) {
        if (user.getGameServer() != null) {
            if (user.getGameServer().isDone()) {
                user.setGameServer(null);
            } else {
                user.getGameServer().doReconnect(user);
            }
        }
    }
}
