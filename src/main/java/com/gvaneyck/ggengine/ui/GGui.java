package com.gvaneyck.ggengine.ui;

import com.gvaneyck.ggengine.game.actions.ActionRef;

import java.util.List;
import java.util.Map;

public interface GGui {
    public void sendMessage(int player, String message);
    public ActionRef resolveChoice(List<ActionRef> actions);
    public void resolveEnd(Map data);
}
