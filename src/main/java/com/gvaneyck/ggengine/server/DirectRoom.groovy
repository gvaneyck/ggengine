package com.gvaneyck.ggengine.server

public class DirectRoom extends Room {

    public DirectRoom(User u1, User u2) {
        super('direct', u1.name + '-' + u2.name)
        join(u1)
        join(u2)
    }
}
