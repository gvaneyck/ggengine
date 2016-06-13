package com.gvaneyck.ggengine.game.actions;

import com.gvaneyck.ggengine.game.GameManager;

import java.lang.reflect.Method;
import java.util.Map;

public class StaticMethodActionRef extends ActionRef {

    private Method method;

    public StaticMethodActionRef(String name, Method method) {
        super(name);
        this.method = method;
    }

    public void invoke(GameManager gm, Map<String, Object> gs, Object[] args) {
        Object[] newArgs = new Object[args.length + 2];
        newArgs[0] = gm;
        newArgs[1] = gs;
        for (int i = 0; i < args.length; i++) {
            newArgs[i + 2] = args[i];
        }

        try {
            method.invoke(null, newArgs);
        }
        catch (Exception e) {
            System.err.println("Error invoking method " + toString());
            e.printStackTrace();
        }
    }
}
