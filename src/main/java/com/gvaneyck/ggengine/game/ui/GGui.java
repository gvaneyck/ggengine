package com.gvaneyck.ggengine.game.ui;

import com.gvaneyck.ggengine.game.actions.ActionOption;

import java.util.List;
import java.util.Map;

public interface GGui {
    public void sendMessage(int player, String message);
    public ActionOption resolveChoice(List<ActionOption> actions);
    public void resolveEnd(Map data);
}
