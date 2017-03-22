package com.gvaneyck.ggengine.game;

import com.gvaneyck.ggengine.game.actions.ActionRef;
import com.gvaneyck.ggengine.game.state.GameStateFilter;
import com.gvaneyck.ggengine.game.ui.GGui;
import com.gvaneyck.ggengine.server.GGException;

import java.util.Map;

public class GameInstanceFactory {

    public static GameInstance getGameInstance(String baseDir, String game, GGui ui, Map<String, Object> initialGameState) {
        GameManager gameManager = GameManager.getInstance(baseDir, game);
        Class gameClass = gameManager.getGameClass();
        Class gsfClass = gameManager.getGsfClass();
        Map<String, ActionRef> actions = gameManager.getActions();

        try {
            return new GameInstance(ui, (Game)gameClass.newInstance(), (GameStateFilter)gsfClass.newInstance(), actions, initialGameState);
        } catch (Exception e) {
            throw new GGException("Error creating GameInstance", null);
        }
    }

}
