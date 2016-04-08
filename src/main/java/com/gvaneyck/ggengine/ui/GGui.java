package com.gvaneyck.ggengine.ui;

import com.gvaneyck.ggengine.Action;

import java.util.List;
import java.util.Map;

public interface GGui {
    public void sendMessage(int player, String message);
    public Action resolveChoice(List<Action> actions);
    public void resolveEnd(Map data);
}
