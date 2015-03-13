package com.gvaneyck.ggengine;


public abstract class Game {
    abstract public void init();

    abstract public void turn();

    abstract public boolean isFinished();

    abstract public void end();
}
