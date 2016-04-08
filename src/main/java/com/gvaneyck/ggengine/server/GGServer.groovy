package com.gvaneyck.ggengine.server

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.java_websocket.WebSocket
import org.java_websocket.drafts.Draft_17
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

public class GGServer extends WebSocketServer {

    Map<WebSocket, User> connections = [:]
    Map<String, User> users = [:]
    Map<String, Map<String, Room>> rooms = [
            'lobby': [ 'General': new LobbyRoom('General') ],
            'game': [:],
            'direct': [:],
    ]

    public GGServer(int port) throws UnknownHostException {
        super(new InetSocketAddress(port), Collections.singletonList(new Draft_17()))
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        println webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + ' CONNECTED'
        connections[webSocket] = new User(webSocket)
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        println webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + ' DISCONNECTED'

        User user = connections.remove(webSocket)
        while (!user.rooms.isEmpty()) {
            Room room = user.rooms[0]
            room.leave(user)
            if (room.users.isEmpty() && room.type != 'lobby') {
                sendToAll([cmd: 'roomDestroy', type: room.type, name: room.name])
            }
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        println webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + ' ' + s

        User user = connections[webSocket]
        def cmd = new JsonSlurper().parseText(s)
        if (commands.containsKey(cmd.cmd)) {
            commands[cmd.cmd](cmd, user)
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        println webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + ' ERROR'
        e.printStackTrace()
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

    def commands = [
            login: { cmd, user ->
                if (user.name != null || cmd.name == null) {
                    return
                }

                String name = cmd.name
                name = name.trim()
                name = name.replaceAll('[^a-zA-Z0-9 ]', '')

                if (users.containsKey(name)) {
                    // Handle existing users
                    def newConn = user.conn
                    user = users[name]
                    user.setConn(newConn)
                    connections.put(newConn, user)
                } else {
                    // Handle new users
                    user.name = name
                    users.put(name, user)
                }

                user.send([cmd: 'login', success: true, name: name])
                user.send([cmd: 'gameList', names: rooms.game.keySet()])
                rooms.lobby['General'].join(user)
                if (user.gameInstance) {
                    if (user.gameInstance.done) {
                        user.gameInstance = null
                    } else {
                        user.gameInstance.doReconnect(user)
                    }
                }
                return
            },
            joinRoom: { cmd, user ->
                if (!rooms.game.containsKey(cmd.name)) {
                    rooms.game[cmd.name] = new GameRoom(cmd)
                    sendToAll([cmd: 'roomCreate', type: 'game', name: cmd.name])
                }

                GameRoom room = rooms.game[cmd.name]
                if (room.password && cmd.password != room.password) {
                    return
                }

                if (room.maxSize == room.users.size()) {
                    return
                }

                room.join(user)
                return
            },
            leaveRoom: { cmd, user ->
                if (!rooms.game.containsKey(cmd.name)) {
                    return
                }

                GameRoom room = rooms.game[cmd.name]
                room.leave(user)
                if (room.users.isEmpty()) {
                    rooms.game.remove(room.name)
                    sendToAll([cmd: 'roomDestroy', type: 'game', name: room.name])
                }
                return
            },
            msg: { cmd, user ->
                // TODO: DM handling
                def room = (rooms[cmd.type] ? rooms[cmd.type][cmd.target] : null)
                if (room) {
                    room.send(new Message(cmd.msg, user.name))
                }
                return
            },
            startGame: { cmd, user ->
                def lobby = lobbyRooms[cmd.name]
                if (lobby.users.size() >= 2 && lobby.users.size() <= 2) {
                    def gameInstance = new GameInstance(lobby)
                    lobby.users.each {
                        gameInstances[it.name] = gameInstance
                    }
                    return
                }
                return
            },
            action: { cmd, user ->
                if (user.gameInstance.done) {
                    user.game = null
                }
                else {
                    user.gameInstance.setChoice(user, cmd.action, cmd.args?.toArray())
                }
            }
    ]
}
