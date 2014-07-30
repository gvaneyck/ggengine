package com.gvaneyck.ggengine;

import java.util.List;
import java.util.Map;

public abstract class Game {
	abstract public void init();
	abstract public void turn();
	abstract public boolean isFinished();
	abstract public void end();
}
