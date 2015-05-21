package com.gvaneyck.ggengine.server

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.java_websocket.WebSocket
import org.java_websocket.drafts.Draft
import org.java_websocket.drafts.Draft_17
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

public class GGServer extends WebSocketServer {

    def gameDir
    def game

    def connections = [:] // WebSocket -> Player
    def players = [:] // String -> Player
    def lobbies = [:] // String -> Lobby

    def gameInstances = [:]

    public GGServer(String gameDir, String game) throws UnknownHostException {
        this(gameDir, game, 9003, new Draft_17())
    }

    public GGServer(String gameDir, String game, int port, Draft d) throws UnknownHostException {
        super(new InetSocketAddress(port), Collections.singletonList(d))
        this.gameDir = gameDir
        this.game = game

        lobbies['General'] = new Lobby(name: 'General')
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        println webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + ' CONNECTED'
        connections[webSocket] = new Player(webSocket)
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        println webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + ' DISCONNECTED'

        Player player = connections.remove(webSocket)
        players.remove(player.name)
        player.lobbies.each {
            it.removePlayer(player)
            if (it.name != 'General' && it.players.isEmpty()) {
                lobbies.remove(it.name)
                sendToAll([cmd: 'lobbyDestroy', name: it.name])
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        println webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + ' ' + s

        Player player = connections[webSocket]
        def cmd = new JsonSlurper().parseText(s)
        if (commands.containsKey(cmd.cmd)) {
            commands[cmd.cmd](cmd, player)
        }
    }

    def commands = [
            setName: { cmd, player ->
                def name = cmd.name
                name = name.trim()
                name = name.replaceAll('[^a-zA-Z0-9 ]', '')
                if (player.name == null && !players.containsKey(name)) {
                    player.name = name
                    players.put(name, player)
                    player.send([cmd: 'nameSelect', success: true, name: name])
                    player.send([cmd: 'lobbyList', names: lobbies.keySet()])
                    lobbies['General'].addPlayer(player)
                    if (gameInstances.containsKey(player.name)) { gameInstances[player.name].doReconnect(player) }
                    return true
                }
                else {
                    player.send([cmd: 'nameSelect', success: false])
                    return false
                }
            },
            makeLobby: { cmd, player ->
                if (lobbies.containsKey(cmd.name)) {
                    return false
                }

                Lobby lobby = new Lobby(cmd)
                lobbies[lobby.name] = lobby
                sendToAll([cmd: 'lobbyCreate', name: lobby.name])

                lobby.addPlayer(player)
                return true
            },
            joinLobby: { cmd, player ->
                if (!lobbies.containsKey(cmd.name)) {
                    return false
                }

                Lobby lobby = lobbies[cmd.name]
                if (lobby.password && cmd.password != lobby.password) {
                    return false
                }

                if (lobby.maxSize == lobby.players.size()) {
                    return false
                }

                lobby.addPlayer(player)
                return true
            },
            leaveLobby: { cmd, player ->
                Lobby lobby = lobbies[cmd.name]
                if (lobby != null) {
                    lobby.removePlayer(player)
                    if (lobby.name != 'General' && lobby.players.isEmpty()) {
                        lobbies.remove(lobby.name)
                        sendToAll([cmd: 'lobbyDestroy', name: lobby.name])
                    }
                }
                return true
            },
            msg: { cmd, player ->
                def target = player.currentChannel //cmd.target
                if (players.containsKey(target)) {
                    Player to = players.get(target)
                    to.sendMsg(player.name, to.name, cmd.msg)
                    player.sendMsg(player.name, to.name, cmd.msg)
                }
                else if (lobbies.containsKey(target)) {
                    lobbies[target].sendMsg(player.name, cmd.msg)
                }
                else {
                    return false
                }
                return true
            },
            startGame: { cmd, player ->
                def lobby = lobbies[cmd.name]
                if (lobby.players.size() >=2 && lobby.players.size() <= 2) {
                    def gameInstance = new GameInstance(lobby)
                    lobby.players.each {
                        gameInstances[it.name] = gameInstance
                    }
                    return true
                }
                return false
            },
            action: { cmd, player ->
                gameInstance.setChoice(player, cmd.action, cmd.args?.toArray())
            }
    ]

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        println webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + ' ERROR'
        e.printStackTrace()
    }

    private Player getPlayer(int playerId) {
        def p = null
        while (p == null) {
            players.each { name, player ->
                if (player.id == playerId) {
                    p = player
                }
            }
            Thread.sleep(1000)
        }
        return p
    }

    private void sendToAll(Map data) {
        Collection<WebSocket> con = connections()
        String dataString = new JsonBuilder(data).toString()
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(dataString)
            }
        }
    }
}
