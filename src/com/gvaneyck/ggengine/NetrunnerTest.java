package com.gvaneyck.ggengine;

import java.util.HashMap;
import java.util.Map;

public class NetrunnerTest {
    public static void main(String[] args) {
        Map<String, Object> gs = new HashMap<String, Object>();
        GameManager gm = new GameManager(gs);
        gm.loadGame("games", "Netrunner");
        gm.gameLoop();
    }
}
