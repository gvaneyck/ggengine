package com.gvaneyck.ggengine.gamestate;

import java.util.Map;

public class PublicGSF implements GameStateFilter {
    public Map filterGameState(Map gs, int player) {
        return gs;
    }
}
