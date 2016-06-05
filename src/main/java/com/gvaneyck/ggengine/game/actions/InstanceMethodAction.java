package com.gvaneyck.ggengine.game.actions;

import com.gvaneyck.ggengine.game.GameManager;

import java.lang.reflect.Method;
import java.util.Map;

public class InstanceMethodAction extends Action {

    private Object instance;
    private Method method;

    public InstanceMethodAction(String clazz, String name, Object instance, Method method) {
        super(clazz, name);
        this.instance = instance;
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
            method.invoke(instance, newArgs);
        }
        catch (Exception e) {
            System.err.println("Error invoking method " + toString());
            e.printStackTrace();
        }
    }
}
