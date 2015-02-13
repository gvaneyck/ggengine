package com.gvaneyck.ggengine.server

import com.gvaneyck.ggengine.Action
import com.gvaneyck.ggengine.gamestate.GameStateSerializer
import com.gvaneyck.ggengine.ui.GGui
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.java_websocket.WebSocket
import org.java_websocket.drafts.Draft
import org.java_websocket.drafts.Draft_76
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

public class GGServer extends WebSocketServer implements GGui {

    def gameDir
    def game

    def connections = [:] // WebSocket -> Player
    def players = [:] // String -> Player
    def lobbies = [:] // String -> Lobby

    def gss = new GameStateSerializer()

    public GGServer(String gameDir, String game) throws UnknownHostException {
        this(gameDir, game, 9003, new Draft_76())
    }

    public GGServer(String gameDir, String game, int port, Draft d) throws UnknownHostException {
        super(new InetSocketAddress(port), Collections.singletonList(d))
        this.gameDir = gameDir
        this.game = game

        Lobby lobby = new Lobby(name: 'General')
        lobbies.put(lobby.name, lobby)
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
                if (player.name == null) {
                    player.name = cmd.name
                    players.put(player.name, player)
                    player.send([cmd: 'lobbyCreate', name: 'General'])
                    lobbies['General'].addPlayer(player)
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

    @Override
    public Action getChoice() {
        System.exit(0)
        return null
    }

    @Override
    public void showChoices(int player, List<Action> actions) {
        List<String> acts = new ArrayList<String>()
        for (Action a : actions)
            acts.add(a.toString())
        sendToAll(gss.serialize(acts))
    }

    @Override
    public void showGS(int player, Map gs) {
        sendToAll(player + " gs: " + gss.serialize(gs))
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
