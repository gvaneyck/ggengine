package com.gvaneyck.ggengine;

import java.lang.reflect.Method;
import java.util.List;

public class Action {

    private int playerId;
    private Object instance;
    private String method;
    private Object[] args;

    public Action(int playerId, Object instance, String method) {
        this.playerId = playerId;
        this.instance = instance;
        this.method = method;
    }

    public Action(int playerId, Object instance, String method, List<Object> args) {
        this(playerId, instance, method, args.toArray());
    }

    public Action(int playerId, Object instance, String method, Object[] args) {
        this.playerId = playerId;
        this.instance = instance;
        this.method = method;
        this.args = args;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getClazz() {
        return instance.getClass().getSimpleName();
    }

    public String getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public boolean matches(int playerId, String name, Object[] args) {
        if (this.args == null || args == null) {
            return (this.args == args);
        }

        if (this.playerId == playerId && method.equals(name) && this.args.length == args.length) {
            boolean matches = true;
            for (int i = 0; i < args.length; i++) {
                if (!this.args[i].equals(args[i])) {
                    matches = false;
                    break;
                }
            }
            return matches;
        }
        else {
            return false;
        }
    }

    public void invoke() {
        try {
            if (args != null) {
                for (Method m : instance.getClass().getMethods()) {
                    if (m.getName().equals(method)) {
                        m.invoke(instance, args);
                        break;
                    }
                }
            }
            else {
                instance.getClass().getMethod(method).invoke(instance);
            }
        }
        catch (Exception e) {
            System.err.println("Error invoking method " + method);
            e.printStackTrace();
        }
    }
}
