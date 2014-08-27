package com.gvaneyck.ggengine.gametests;

import com.gvaneyck.ggengine.GameManager;

import java.util.HashMap;
import java.util.Map;

public class TicTacToeTest {
    public static void main(String[] args) {
        Map<String, Object> gs = new HashMap<String, Object>();
        GameManager gm = new GameManager(gs);
        gm.loadGame("games", "TicTacToe");
        gm.gameLoop();
    }
}
