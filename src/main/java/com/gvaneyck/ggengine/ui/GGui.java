package com.gvaneyck.ggengine.ui;

import com.gvaneyck.ggengine.Action;

import java.util.List;

public interface GGui {
    public Action resolveChoice(List<Action> actions);
}
