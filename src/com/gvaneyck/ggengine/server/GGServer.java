package com.gvaneyck.ggengine.server;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collections;

public class GGServer extends WebSocketServer {

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
}
