package com.gvaneyck.ggengine.gametests;

import com.gvaneyck.ggengine.GameManager;
import com.gvaneyck.ggengine.server.GGServer;

import java.util.HashMap;
import java.util.Map;

public class SplendorTest {
    public static void main(String[] args) {
        basicTest();
    }

    public static void basicTest() {
        Map gameSettings = new HashMap<String, Object>();
        gameSettings.put("players", 2);

        GameManager gm = new GameManager(gameSettings);
        gm.loadGame("games", "Splendor");
        gm.gameLoop();
    }

    public static void webTest() {
        try {
            final GGServer ggs = new GGServer("games", "Splendor");
            ggs.start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
