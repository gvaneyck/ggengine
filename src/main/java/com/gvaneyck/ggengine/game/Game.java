package com.gvaneyck.ggengine.game;

import java.util.Map;

public interface Game {
    void init(GameManager gm, Map<String, Object> gs);
    void turn(GameManager gm, Map<String, Object> gs);
    boolean isFinished(GameManager gm, Map<String, Object> gs);
    Map end(GameManager gm, Map<String, Object> gs);
}
