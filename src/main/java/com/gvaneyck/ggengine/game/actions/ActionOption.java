package com.gvaneyck.ggengine.game.actions;

import com.gvaneyck.ggengine.game.GameManager;

import java.util.List;
import java.util.Map;

public class ActionOption {

    private int playerId;
    private ActionRef actionRef;
    private Object[] args; // TODO: action ranges

    public ActionOption(int playerId, ActionRef actionRef) {
        this(playerId, actionRef, null);
    }

    public ActionOption(int playerId, ActionRef actionRef, List<Object> args) {
        this.playerId = playerId;
        this.actionRef = actionRef;
        if (args != null) {
            this.args = args.toArray();
        }
    }

    public int getPlayerId() {
        return playerId;
    }

    public ActionRef getActionRef() {
        return actionRef;
    }

    public Object[] getArgs() {
        return args;
    }

    public boolean matches(int playerId, String action, Object[] args) {
        if (this.playerId != playerId) {
            return false;
        }

        if (!this.actionRef.toString().equals(action)) {
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

    public String toString() {
        String result = playerId + "." + actionRef.toString() + "(";
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                result += ", ";
            }
            result += args[i];
        }
        result += ")";
        return result;
    }
}
