package com.gvaneyck.ggengine.game.actions;

import com.gvaneyck.ggengine.game.GameManager;

import java.util.Map;

public abstract class ActionRef {

    private String name;

    public ActionRef(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean equals(ActionRef other) {
        return name.equals(other.getName());
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        return name;
    }

    public abstract void invoke(GameManager gm, Map<String, Object> gs, Object[] args);
}
