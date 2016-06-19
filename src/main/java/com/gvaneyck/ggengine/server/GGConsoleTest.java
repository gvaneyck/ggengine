package com.gvaneyck.ggengine.server;

import com.gvaneyck.ggengine.game.GameManager;
import com.gvaneyck.ggengine.ui.ConsoleUI;

import java.util.HashMap;
import java.util.Map;

public class GGConsoleTest {
    public static void main(String[] args) {
        Map<String, Object> initialGameState = new HashMap<>();
        initialGameState.put("players", 2);
        new GameManager("games", "TicTacToe").getGameInstance(new ConsoleUI(), initialGameState).startGame();
    }
}
