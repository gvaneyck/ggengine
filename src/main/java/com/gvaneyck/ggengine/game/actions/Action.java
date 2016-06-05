package com.gvaneyck.ggengine.game.actions;

import com.gvaneyck.ggengine.game.GameManager;

import java.util.Map;

public abstract class Action {

    private String clazz;
    private String name;

    private String fullName;
    private Integer hash = null;

    public Action(String clazz, String name) {
        this.clazz = clazz;
        this.name = name;
        this.fullName = clazz + "." + name;
    }

    public String getClazz() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Action other) {
        if (!clazz.equals(other.getClazz())) {
            return false;
        }

        if (!name.equals(other.getName())) {
            return false;
        }

        return true;
    }

    public String toString() {
        return fullName;
    }

    public abstract void invoke(GameManager gm, Map<String, Object> gs, Object[] args);
}
