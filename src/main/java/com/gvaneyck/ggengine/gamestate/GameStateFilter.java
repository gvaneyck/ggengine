package com.gvaneyck.ggengine.gamestate;

import java.util.Map;

public interface GameStateFilter {
    public Map<String, Object> filterGameState(Map<String, Object> gs, int player);
}
