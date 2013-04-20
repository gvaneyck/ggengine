package com.gvaneyck.ggengine.gui;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

public class GGEngine extends Canvas implements Runnable {
	private static final long serialVersionUID = 1L;

	public final String TITLE = "GGEngine";
	public final int WIDTH = 640;
	public final int HEIGHT = WIDTH / 16 * 10;
	
	public final long UPDATE_RATE = 60;
	public final long FPS_INTERVAL = 1000000000;
	public final long UPDATE_INTERVAL = 1000000000 / UPDATE_RATE;

	private Thread thread;
	private boolean running = false;
	private boolean paused = false;
	private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
	private JFrame frame;
	private int time = 0;

	public GGEngine() {
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
		}
		catch (InterruptedException e) {
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
				if (!paused) update();
				updates++;
				lastUpdate += UPDATE_INTERVAL;
			}

			render();
			frames++;

			if (now - lastFPS > FPS_INTERVAL) {
				frame.setTitle(TITLE + "  |  " + updates + " ups, " + frames + " fps");
				frames = 0;
				updates = 0;
				lastFPS += FPS_INTERVAL;
			}
		}
	}

	public void update() {
		time++;
		if (time == 65536) time = 0;
	}

	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		//screen.graphics(g);
		//screen.clear();
		for (int i = 0; i < WIDTH * HEIGHT; i++) {
			//pixels[i] = screen.pixels[i];
		}
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		
		g.dispose();
		bs.show();
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
