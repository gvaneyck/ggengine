package com.gvaneyck.ggengine.gamestate;

import java.util.Map;

public interface GameStateFilter {
    public Map filterGameState(Map gs, int player);
}
