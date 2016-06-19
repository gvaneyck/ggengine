package com.gvaneyck.ggengine.gamestate;

import java.util.Map;

public class PublicGSF implements GameStateFilter {
    public Map<String, Object> filterGameState(Map<String, Object> gs, int player) {
        return gs;
    }
}
