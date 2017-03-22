package com.gvaneyck.ggengine.game.actions;

import com.gvaneyck.ggengine.game.GameInstance;
import com.gvaneyck.ggengine.server.GGException;

import java.lang.reflect.Method;
import java.util.Map;

public class InstanceMethodActionRef extends ActionRef {

    private Object instance;
    private Method method;

    public InstanceMethodActionRef(String name, Object instance, Method method) {
        super(name);
        this.instance = instance;
        this.method = method;
    }

    public void invoke(GameInstance gm, Map<String, Object> gs, Object[] args) {
        Object[] newArgs = new Object[args.length + 2];
        newArgs[0] = gm;
        newArgs[1] = gs;
        for (int i = 0; i < args.length; i++) {
            newArgs[i + 2] = args[i];
        }

        try {
            method.invoke(instance, newArgs);
        } catch (Exception e) {
            throw new GGException("Error invoking method " + toString(), e);
        }
    }
}
