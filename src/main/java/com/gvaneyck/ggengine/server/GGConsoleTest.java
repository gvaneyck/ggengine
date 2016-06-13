package com.gvaneyck.ggengine.server;

import com.gvaneyck.ggengine.game.GameManager;
import com.gvaneyck.ggengine.ui.ConsoleUI;

import java.util.HashMap;
import java.util.Map;

public class GGConsoleTest {
    public static void main(String[] args) {
        Map<String, Object> gameArgs = new HashMap<>();
        gameArgs.put("players", 2);
        new GameManager(gameArgs, new ConsoleUI(), "games", "TicTacToe").gameLoop();
    }
}
