package com.gvaneyck.ggengine;

import java.lang.reflect.Method;
import java.util.List;

public class Action {
    public int playerId;
    public Object instance;
    public String method;
    public Object[] args;

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
            System.err.println("Error invoking method " + this.toString());
            e.printStackTrace();
        }
    }

    public String toString() {
        Class iClass = instance.getClass();
        String clazz = iClass.getSimpleName();
        try {
            if (iClass.getMethod("toString", null).getDeclaringClass().equals(iClass)) {
                clazz = instance.toString();
            }
        }
        catch (Exception e) { }

        StringBuilder buffer = new StringBuilder();
        buffer.append(playerId);
        buffer.append('-');
        buffer.append(clazz);
        buffer.append('.');
        buffer.append(method);
        buffer.append('(');
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                buffer.append(args[i]);
                if (i < args.length - 1) {
                    buffer.append(", ");
                }
            }
        }
        buffer.append(')');
        return buffer.toString();
    }
}
