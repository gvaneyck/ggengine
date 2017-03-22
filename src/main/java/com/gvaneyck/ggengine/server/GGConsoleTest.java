package com.gvaneyck.ggengine.server;

import com.gvaneyck.ggengine.game.GameInstanceFactory;
import com.gvaneyck.ggengine.ui.ConsoleUI;

import java.util.LinkedHashMap;
import java.util.Map;

public class GGConsoleTest {
    public static void main(String[] args) {
        Map<String, Object> initialGameState = new LinkedHashMap<>();
        initialGameState.put("players", 2);
        GameInstanceFactory.getGameInstance("games", "TicTacToe", new ConsoleUI(), initialGameState).startGame();
    }
}
