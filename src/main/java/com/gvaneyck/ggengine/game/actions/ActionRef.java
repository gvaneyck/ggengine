package com.gvaneyck.ggengine.game.actions;

import com.gvaneyck.ggengine.game.GameManager;

import java.util.List;
import java.util.Map;

public class ActionRef {

    private int playerId;
    private Action action;
    private Object[] args; // TODO: action ranges

    public ActionRef(int playerId, Action action) {
        this(playerId, action, null);
    }

    public ActionRef(int playerId, Action action, List<Object> args) {
        this.playerId = playerId;
        this.action = action;
        if (args != null) {
            this.args = args.toArray();
        }
    }

    public int getPlayerId() {
        return playerId;
    }

    public Action getAction() {
        return action;
    }

    public Object[] getArgs() {
        return args;
    }

    public boolean matches(int playerId, String action, Object[] args) {
        if (this.playerId != playerId) {
            return false;
        }

        if (!this.action.toString().equals(action)) {
            return false;
        }

        if (this.args.length != args.length) {
            return false;
        }

        for (int i = 0; i < this.args.length; i++) {
            if (!this.args[i].equals(args[i])) {
                return false;
            }
        }

        return true;
    }

    public void invoke(GameManager gm, Map<String, Object> gs) {
        action.invoke(gm, gs, args);
    }
}
