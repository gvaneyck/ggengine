package com.gvaneyck.ggengine.gui;

import java.awt.*;

public abstract class Screen {
    protected int[] pixels;
    protected int width;
    protected int height;

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new int[width * height];
    }

    public int[] render() {
        return null;
    }

    public void doGraphics(Graphics g) {
    }
}
