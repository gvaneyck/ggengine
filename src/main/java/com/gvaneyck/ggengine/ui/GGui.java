package com.gvaneyck.ggengine.ui;

import com.gvaneyck.ggengine.Action;

import java.util.List;
import java.util.Map;

public interface GGui {
    public Action resolveChoice(List<Action> actions);
    public void resolveEnd(Map data);
}
