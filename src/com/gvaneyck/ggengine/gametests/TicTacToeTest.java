package com.gvaneyck.ggengine.gametests;

import com.gvaneyck.ggengine.GameManager;

public class TicTacToeTest {
    public static void main(String[] args) {
        GameManager gm = new GameManager();
        gm.loadGame("games", "TicTacToe");
        gm.gameLoop();
    }
}
