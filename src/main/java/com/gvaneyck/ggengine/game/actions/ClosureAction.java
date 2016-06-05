package com.gvaneyck.ggengine.game.actions;

import com.gvaneyck.ggengine.game.GameManager;
import groovy.lang.Closure;

import java.util.Map;

public class ClosureAction extends Action {

    private Closure closure;

    public ClosureAction(String clazz, String method, Closure closure) {
        super(clazz, method);
        this.closure = closure;
    }

    public void invoke(GameManager gm, Map<String, Object> gs, Object[] args) {
        Object[] newArgs = new Object[args.length + 2];
        newArgs[0] = gm;
        newArgs[1] = gs;
        for (int i = 0; i < args.length; i++) {
            newArgs[i + 2] = args[i];
        }

        closure.call(newArgs);
    }
}
