package com.gvaneyck.ggengine.game;

import com.gvaneyck.ggengine.game.actions.ActionOption;
import com.gvaneyck.ggengine.game.actions.ActionRef;
import com.gvaneyck.ggengine.gamestate.GameStateFilter;
import com.gvaneyck.ggengine.ui.GGui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameInstance {

    private boolean started = false;

    private GGui ui;
    private Game game;
    private GameStateFilter gsf;

    private List<ActionOption> pendingActions = new ArrayList<>();
    private Map<String, ActionRef> actions;
    private Map<String, Object> gs;

    private AccessibleRandom internalRand = new AccessibleRandom();
    public Random rand = internalRand.getRand();

    public GameInstance(GGui ui, Game game, GameStateFilter gsf, Map<String, ActionRef> actions, Map<String, Object> initialGameState) {
        this.ui = ui;
        this.game = game;
        this.gsf = gsf;
        this.actions = actions;
        this.gs = initialGameState;
    }

    public void addAction(int player, String name) {
        addAction(player, name, null);
    }

    public void addAction(int player, String name, List<Object> args) {
        ActionRef actionRef = actions.get(name);
        if (actionRef == null) {
            System.err.println("No action found for " + name);
        } else {
            pendingActions.add(new ActionOption(player, actionRef, args));
        }
    }

    public void resolveAllActions() {
        while (!pendingActions.isEmpty()) {
            ActionOption option = ui.resolveChoice(pendingActions);

            // Remove player's actions from pending in case resolveActions is called again during action resolution
            Iterator<ActionOption> it = pendingActions.iterator();
            while (it.hasNext()) {
                if (it.next().getPlayerId() == option.getPlayerId()) {
                    it.remove();
                }
            }

            option.getActionRef().invoke(this, gs, option.getArgs());
        }
    }

    public void resolveOneAction() {
        ActionOption option = ui.resolveChoice(pendingActions);
        pendingActions.clear();
        option.getActionRef().invoke(this, gs, option.getArgs());
    }

    public void sendMessage(String message) {
        for (int i = 1; i <= (Integer)gs.get("players"); i++) {
            sendMessage(i, message);
        }
    }

    public void sendMessage(int player, String message) {
        ui.sendMessage(player, message);
    }

    public void startGame() {
        if (started) {
            System.err.println("Tried to start an already started game");
            return;
        }

        game.init(this, gs);
        while (!game.isFinished(this, gs)) {
            game.turn(this, gs);
        }
        ui.resolveEnd(game.end(this, gs));
    }

    public Map<String, Object> getGameState(int player) {
        Map<String, Object> gameState = gsf.filterGameState(gs, player);
        gameState.put("me", player);
        return gameState;
    }
}
