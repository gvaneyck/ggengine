package com.gvaneyck.ggengine;

import java.lang.reflect.Method;
import java.util.List;

public class Action {
    public Object instance;
    public String method;
    public Object[] args;

    public Action(Object instance, String method) {
        this.instance = instance;
        this.method = method;
    }

    public Action(Object instance, String method, Object[] args) {
        this.instance = instance;
        this.method = method;
        this.args = args;
    }

    public Action(Object instance, String method, List<Object> args) {
        this(instance, method, args.toArray());
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
        String clazz = instance.getClass().getSimpleName();
        if (args == null) {
            return clazz + "." + method + "()";
        }

        StringBuilder buffer = new StringBuilder(clazz);
        buffer.append('.');
        buffer.append(method);
        buffer.append('(');
        for (int i = 0; i < args.length; i++) {
            buffer.append(args[i]);
            if (i < args.length - 1) {
                buffer.append(", ");
            }
        }
        buffer.append(')');
        return buffer.toString();
    }
}
