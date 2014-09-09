package com.gvaneyck.ggengine.server;

import com.gvaneyck.ggengine.Action;
import com.gvaneyck.ggengine.GameManager;
import com.gvaneyck.ggengine.gamestate.GameStateSerializer;
import com.gvaneyck.ggengine.ui.GGui;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_76;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GGServer extends WebSocketServer implements GGui {

    private String dir;
    private String game;

    private Map<WebSocket, Player> connections = new HashMap<WebSocket, Player>();
    private Map<String, Player> players = new HashMap<String, Player>();
    private Map<String, Lobby> lobbies = new HashMap<String, Lobby>();


    private GameStateSerializer gss = new GameStateSerializer();

    public GGServer(String dir, String game) throws UnknownHostException {
        this(dir, game, 9003, new Draft_76());
    }

    public GGServer(String dir, String game, int port, Draft d) throws UnknownHostException {
        super(new InetSocketAddress(port), Collections.singletonList(d));
        this.dir = dir;
        this.game = game;

        Lobby lobby = new Lobby(null, "General");
        lobbies.put(lobby.name, lobby);
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println(webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + " CONNECTED");
        connections.put(webSocket, new Player(webSocket));
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println(webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + " DISCONNECTED");

        Player p = connections.remove(webSocket);
        players.remove(p.name);
        for (Lobby lobby : p.lobbies) {
            lobby.players.remove(p);
        }
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println(webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + " " + s);

        Player p = connections.get(webSocket);
        if (s.startsWith("setName")) {
            if (p.name == null) {
                p.name = s.substring(8);
                players.put(p.name, p);
                lobbies.get("General").players.add(p);
            }
        }
        else if (s.startsWith("makeLobby")) {
            String[] parts = s.split(",");
            if (parts.length < 3) {
                return;
            }

            if (lobbies.containsKey(parts[1])) {
                return;
            }

            Lobby lobby = new Lobby(parts[1], parts[2]);
            if (parts.length > 3) {
                lobby.password = parts[3];
            }
            lobby.players.add(p);
            lobbies.put(lobby.name, lobby);
        }
        else if (s.startsWith("joinLobby")) {
            String[] parts = s.split(",");
            if (parts.length < 2) {
                return;
            }

            if (!lobbies.containsKey(parts[1])) {
                return;
            }

            Lobby lobby = lobbies.get(parts[1]);
            if (parts.length > 2) {
                if (lobby.password.equals(parts[2])) {
                    lobby.players.add(p);
                }
            }
            else {
                lobby.players.add(p);
            }
        }
        else if (s.startsWith("msg")) {
            String[] parts = s.split(",");
            if (parts.length < 3) {
                return;
            }

            String target = parts[1];
            String msg = parts[2];

            if (players.containsKey(target)) {
                Player to = players.get(target);
                to.sendMsg(p.name, to.name, msg);
                p.sendMsg(p.name, to.name, msg);
            }
            else if (lobbies.containsKey(target)) {
                lobbies.get(target).sendMsg(p.name, msg);
            }
        }
        else if (s.startsWith("startGame")) {
            final GGui ggs = this;
            Thread t = new Thread() {
                public void run() {
                    GameManager gm = new GameManager(new HashMap<String, Object>(), ggs);
                    gm.loadGame(dir, game);
                    gm.gameLoop();
                }
            };
            t.start();
        }
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println(webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + " ERROR");
        e.printStackTrace();
    }

    @Override
    public Action getChoice() {
        System.exit(0);
        return null;
    }

    @Override
    public void showChoices(int player, List<Action> actions) {
        List<String> acts = new ArrayList<String>();
        for (Action a : actions)
            acts.add(a.toString());
        sendToAll(gss.serialize(acts));
    }

    @Override
    public void showGS(int player, Map gs) {
        sendToAll(player + " gs: " + gss.serialize(gs));
    }

    private void sendToAll(String text) {
        while (connections().isEmpty()) {
            try { Thread.sleep(1000); } catch (Exception e) { }
        }

        Collection<WebSocket> con = connections();
        synchronized (con) {
            for (WebSocket c : con) {
                c.send(text);
            }
        }
    }
}
