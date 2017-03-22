package com.gvaneyck.ggengine.game.state;

import java.util.Map;

public interface GameStateFilter {
    public Map<String, Object> filterGameState(Map<String, Object> gs, int player);
}
