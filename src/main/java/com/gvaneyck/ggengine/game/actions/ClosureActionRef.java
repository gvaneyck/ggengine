package com.gvaneyck.ggengine.game.actions;

import com.gvaneyck.ggengine.game.GameManager;
import groovy.lang.Closure;

import java.util.Map;

public class ClosureActionRef extends ActionRef {

    private Closure closure;

    public ClosureActionRef(String name, Closure closure) {
        super(name);
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
