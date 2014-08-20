package com.gvaneyck.ggengine;

import java.util.HashMap;
import java.util.Map;

public class NetrunnerTest {
    public static void main(String[] args) {
        Map<String, Object> gs = new HashMap<String, Object>();
        GameManager2 gm = new GameManager2(gs);
        gm.loadClasses("Netrunner");
        gm.gameLoop();
    }
}
