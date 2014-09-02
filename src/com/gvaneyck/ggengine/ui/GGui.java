package com.gvaneyck.ggengine.ui;

import com.gvaneyck.ggengine.Action;

import java.util.List;
import java.util.Map;

public interface GGui {
    public Action getChoice();
    public void showChoices(List<Action> actions);
    public void showGS(int player, Map gs);
}
