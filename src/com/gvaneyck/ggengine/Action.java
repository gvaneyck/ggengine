package com.gvaneyck.ggengine;

import java.util.List;

public class Action {
    public String clazz;
    public Object[] args;

    public Action(String clazz) {
        this.clazz = clazz;
    }

    public Action(String clazz, Object[] args) {
        this.clazz = clazz;
        this.args = args;
    }

    public Action(String clazz, List<Object> args) {
        this.clazz = clazz;
        this.args = args.toArray();
    }

    public String toString() {
        if (args == null)
            return clazz + "()";

        StringBuilder buffer = new StringBuilder(clazz);
        buffer.append('(');
        for (int i = 0; i < args.length; i++) {
            buffer.append(args[i]);
            if (i < args.length - 1)
                buffer.append(", ");
        }
        buffer.append(')');
        return buffer.toString();
    }
}
