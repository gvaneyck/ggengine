package com.gvaneyck.ggengine.game.ui;

import com.gvaneyck.ggengine.game.actions.ActionOption;

import java.util.List;
import java.util.Map;

public interface GGui {
    ActionOption resolveChoice(List<ActionOption> actions);
    void sendMessage(int player, String message);
    void resolveEnd(Map data);
}
