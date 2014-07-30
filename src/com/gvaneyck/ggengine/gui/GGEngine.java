package com.gvaneyck.ggengine.gui;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

public class GGEngine extends Canvas implements Runnable {
    private static final long serialVersionUID = -3534599314003483547L;

    public final String TITLE = "GGEngine";
    public final int WIDTH = 640;
    public final int HEIGHT = WIDTH * 10 / 16;

    public final long UPDATE_RATE = 60;
    public final long FPS_INTERVAL = 1000000000;
    public final long UPDATE_INTERVAL = 1000000000 / UPDATE_RATE;

    public JFrame frame;

    private Thread thread;
    private boolean running = false;
    private boolean paused = false;
    private BufferedImage image;
    private int[] pixels;
    private Screen screen;

    public GGEngine() {
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        Dimension size = new Dimension(WIDTH, HEIGHT);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
    }

    public void start() {
        running = true;
        thread = new Thread(this, "GameThread");
        thread.start();
    }

    public void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        long lastFPS = System.nanoTime();
        long lastUpdate = lastFPS;
        int frames = 0;
        int updates = 0;

        requestFocus();
        while (running) {
            long now = System.nanoTime();

            if (now - lastUpdate >= UPDATE_INTERVAL) {
                updates++;
                lastUpdate += UPDATE_INTERVAL;

                render();
                frames++;
            }

            if (now - lastFPS > FPS_INTERVAL) {
                frame.setTitle(String.format("%s | %d ups, %s fps", TITLE, updates, frames));
                frames = 0;
                updates = 0;
                lastFPS += FPS_INTERVAL;
            }
        }
    }

    public void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        for (int i = 0; i < WIDTH * HEIGHT; i++)
            pixels[i] = 0;

        Graphics g = bs.getDrawGraphics();

        if (screen != null) {
            int[] drawPixels = screen.render();
            if (drawPixels != null && drawPixels.length == WIDTH * HEIGHT) {
                for (int i = 0; i < WIDTH * HEIGHT; i++)
                    pixels[i] = drawPixels[i];
            }
        }

        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);

        if (screen != null)
            screen.doGraphics(g);

        g.dispose();
        bs.show();
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
        screen.setSize(getWidth(), getHeight());
    }

    public static void main(String[] args) {
        GGEngine engine = new GGEngine();
        engine.frame = new JFrame();
        engine.frame.setResizable(false);
        engine.frame.setTitle(engine.TITLE);
        engine.frame.add(engine);
        engine.frame.pack();
        engine.frame.setLocationRelativeTo(null);
        engine.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        engine.frame.setVisible(true);

        engine.start();
    }

}
