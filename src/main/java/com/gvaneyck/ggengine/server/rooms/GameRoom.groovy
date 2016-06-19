package com.gvaneyck.ggengine.server.rooms

import com.gvaneyck.ggengine.server.GameServer

public class GameRoom extends Room {

    String game
    String password
    Integer minSize
    Integer maxSize
    GameServer gameInstance

    public GameRoom(Map args) {
        this((String)args.name, (String)args.game, (String)args.password, (Integer)args.minSize, (Integer)args.maxSize)
    }

    public GameRoom(String name, String game, String password, Integer minSize, Integer maxSize) {
        super('game', name)

        this.name = name
        this.game = game
        this.password = password
        this.minSize = minSize
        this.maxSize = maxSize
    }

    public boolean canStart() {
        return (!gameInstance
                && (!minSize || minSize <= users.size())
                && (!maxSize || maxSize >= users.size()))
    }
}
