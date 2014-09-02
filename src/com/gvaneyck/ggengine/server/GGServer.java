package com.gvaneyck.ggengine.server;

import com.gvaneyck.ggengine.Action;
import com.gvaneyck.ggengine.gamestate.GameStateSerializer;
import com.gvaneyck.ggengine.ui.GGui;
import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GGServer extends WebSocketServer implements GGui {

    private GameStateSerializer gss = new GameStateSerializer();

    public GGServer(int port, Draft d) throws UnknownHostException {
        super(new InetSocketAddress(port), Collections.singletonList(d));
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println(webSocket);
        System.out.println("Opened socket from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
        System.out.println(webSocket);
        System.out.println("Closed socket (" + s + ") from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {
        System.out.println(webSocket);
        System.out.println("Received message from " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress() + ": " + s);
    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {
        System.out.println(webSocket);
        System.out.println("Encountered error with " + webSocket.getRemoteSocketAddress().getAddress().getHostAddress());
        e.printStackTrace();
    }

    @Override
    public Action getChoice() {
        System.exit(0);
        return null;
    }

    @Override
    public void showChoices(List<Action> actions) {
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
