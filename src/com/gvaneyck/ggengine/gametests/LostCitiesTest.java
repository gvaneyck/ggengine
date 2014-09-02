package com.gvaneyck.ggengine.gametests;

import com.gvaneyck.ggengine.GameManager;
import com.gvaneyck.ggengine.server.GGServer;
import org.java_websocket.drafts.Draft_76;

import java.util.HashMap;

public class LostCitiesTest {
    public static void main(String[] args) {
        webTest();
    }

    public static void basicTest() {
        GameManager gm = new GameManager();
        gm.loadGame("games", "LostCities");
        gm.gameLoop();
    }

    public static void webTest() {
        try {
            final GGServer ggs = new GGServer(9003, new Draft_76());
            Thread t = new Thread() {
                public void run() {
                    try { ggs.start(); } catch (Exception e) { e.printStackTrace(); }
                }
            };
            t.start();

            GameManager gm = new GameManager(new HashMap<String, Object>(), ggs);
            gm.loadGame("games", "LostCities");
            gm.gameLoop();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
