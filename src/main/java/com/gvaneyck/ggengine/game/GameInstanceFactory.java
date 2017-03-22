package com.gvaneyck.ggengine.game;

import com.gvaneyck.ggengine.game.actions.ActionRef;
import com.gvaneyck.ggengine.gamestate.GameStateFilter;
import com.gvaneyck.ggengine.ui.GGui;

import java.util.Map;

public class GameInstanceFactory {

    public static GameInstance getGameInstance(String baseDir, String game, GGui ui, Map<String, Object> initialGameState) {
        GameManager gameManager = GameManager.getInstance(baseDir, game);
        Class gameClass = gameManager.getGameClass();
        Class gsfClass = gameManager.getGsfClass();
        Map<String, ActionRef> actions = gameManager.getActions();

        if (gameClass == null) {
            System.err.println("Unable to find Game class for " + baseDir + "/" + game);
            return null;
        }

        try {
            return new GameInstance(ui, (Game)gameClass.newInstance(), (GameStateFilter)gsfClass.newInstance(), actions, initialGameState);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
