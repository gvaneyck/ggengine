package com.gvaneyck.ggengine.server

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.java_websocket.WebSocket
import org.java_websocket.drafts.Draft
import org.java_websocket.drafts.Draft_76
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

public class GGServer extends WebSocketServer {

    def gameDir
    def game

    def connections = [:] // WebSocket -> Player
    def players = [:] // String -> Player
    def lobbies = [:] // String -> Lobby

    def gameInstance

    public GGServer(String gameDir, String game) throws UnknownHostException {
        this(gameDir, game, 9003, new Draft_76())
    }

    public GGServer(String gameDir, String game, int port, Draft d) throws UnknownHostException {
        super(new InetSocketAddress(port), Collections.singletonList(d))
        this.gameDir = gameDir
        this.game = game

        lobbies['General'] = new Lobby(name: 'General')

        gameInstance = new GameInstance('foobar')
        gameInstance.playerIds = ['Jabe1': 1, 'Jabe2': 2]
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
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        println webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + ' ' + s

        Player player = connections[webSocket]
        def cmd = new JsonSlurper().parseText(s)
        switch (cmd.cmd) {
            case 'setName':
                def name = cmd.name
                name = name.trim()
                name = name.replaceAll('[^a-zA-Z0-9 ]', '')
                if (player.name == null && !players.containsKey(name)) {
                    player.name = name
                    players.put(name, player)
                    player.send([cmd: 'nameSelect', success: true, name: name])
                    player.send([cmd: 'lobbyList', names: lobbies.keySet()])
                    lobbies['General'].addPlayer(player)
                    gameInstance.doReconnect(player)
                }
                else {
                    player.send([cmd: 'nameSelect', success: false])
                }
                break

            case 'makeLobby':
                if (lobbies.containsKey(cmd.name)) {
                    break
                }

                Lobby lobby = new Lobby(cmd)
                lobbies[lobby.name] = lobby
                sendToAll([cmd: 'lobbyCreate', name: lobby.name])

                lobby.addPlayer(player)
                break

            case 'joinLobby':
                if (!lobbies.containsKey(cmd.name)) {
                    break
                }

                Lobby lobby = lobbies[cmd.name]
                if (lobby.password && cmd.password != lobby.password) {
                    break
                }

                lobby.addPlayer(player)
                break

            case 'msg':
                def target = cmd.target
                if (players.containsKey(target)) {
                    Player to = players.get(target)
                    to.sendMsg(player.name, to.name, cmd.msg)
                    player.sendMsg(player.name, to.name, cmd.msg)
                } else if (lobbies.containsKey(target)) {
                    lobbies[target].sendMsg(player.name, cmd.msg)
                }
                break

            case 'action':
                gameInstance.setChoice(player, cmd.action, cmd.args?.toArray())
        }
//        else if (s.startsWith("startGame")) {
//            final GGui ggs = this
//            Thread t = new Thread() {
//                public void run() {
//                    GameManager gm = new GameManager(new HashMap<String, Object>(), ggs)
//                    gm.loadGame(gameDir, game)
//                    gm.gameLoop()
//                }
//            }
//            t.start()
//        }
    }

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
