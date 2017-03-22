package com.gvaneyck.ggengine.game.state;

import java.util.Map;

public class PublicGSF implements GameStateFilter {
    public Map<String, Object> filterGameState(Map<String, Object> gs, int player) {
        return gs;
    }
}
