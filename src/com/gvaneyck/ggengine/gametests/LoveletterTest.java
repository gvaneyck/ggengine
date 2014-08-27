package com.gvaneyck.ggengine.gametests;

import com.gvaneyck.ggengine.GameManager;

import java.util.HashMap;
import java.util.Map;

public class LoveletterTest {

    public static void main(String[] args) {
        Map<String, Object> gs = new HashMap<String, Object>();
        gs.put("maxPlayers", 2);
        GameManager gm = new GameManager(gs);
        gm.loadGame("games", "LoveLetter");
        gm.gameLoop();
    }
}
