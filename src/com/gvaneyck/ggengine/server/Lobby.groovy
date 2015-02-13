package com.gvaneyck.ggengine.server

public class Lobby {
    def name
    def game
    def password

    def players = []

    public Lobby(Map params) {
        this(params.name, params.game, params.password)
    }

    public Lobby(name, game, password) {
        this.name = name
        this.game = game
        this.password = password
    }

    public void addPlayer(Player p) {
        // TODO synchronize
        players.each {
            it.send([cmd: 'lobbyJoin', name: name, members: [p.name]])
        }

        players << p
        p.lobbies << this

        p.send([cmd: 'lobbyJoin', name: name, members: players.name])
    }

    public void removePlayer(Player p) {
        players.each {
            it.send([cmd: 'lobbyLeave', name: name, member: p.name])
        }
        players.remove(p)
    }

    public void sendMsg(String from, String msg) {
        players.each {
            it.sendMsg(from, name, msg)
        }
    }
}
