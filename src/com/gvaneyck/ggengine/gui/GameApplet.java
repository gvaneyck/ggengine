package com.gvaneyck.ggengine.gui;

import java.applet.Applet;
import java.awt.*;

public class GameApplet extends Applet {
    private static final long serialVersionUID = -3544802690382500633L;

    GGEngine engine = new GGEngine();

    public void init() {
        setLayout(new BorderLayout());
        add(engine);
    }

    public void start() {
        engine.start();
    }

    public void stop() {
        engine.stop();
    }
}
