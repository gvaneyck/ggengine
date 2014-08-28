package com.gvaneyck.ggengine.server;

import org.java_websocket.drafts.Draft_76;

public class GGServerTest {
    public static void main(String[] args) {
        try {
            //WebSocketImpl.DEBUG = false;
            new GGServer(9003, new Draft_76()).start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
