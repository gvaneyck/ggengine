package com.gvaneyck.ggengine;

import java.util.HashMap;
import java.util.Map;

public class LoveletterTest {

    public static void main(String[] args) {
        Map<String, Object> gs = new HashMap<String, Object>();
        gs.put("maxPlayers", 2);
        GameManager2 gm = new GameManager2(gs);
        gm.loadClasses("LoveLetter");
        gm.gameLoop();
    }
}
