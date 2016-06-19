package com.gvaneyck.ggengine.game;

import java.util.Map;

public interface Game {
    void init(GameInstance gm, Map<String, Object> gs);
    void turn(GameInstance gm, Map<String, Object> gs);
    boolean isFinished(GameInstance gm, Map<String, Object> gs);
    Map end(GameInstance gm, Map<String, Object> gs);
}
