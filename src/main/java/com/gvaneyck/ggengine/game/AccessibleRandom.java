package com.gvaneyck.ggengine.game;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class AccessibleRandom {
    private static Field seedField;

    static {
        try {
            seedField = Random.class.getDeclaredField("seed");
            seedField.setAccessible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Random rand = new Random();

    public long getTheSeed() {
        long seed = 0;
        try {
            seed = ((AtomicLong)seedField.get(rand)).get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return seed;
    }

    public void setTheSeed(long seed) {
        try {
            ((AtomicLong)seedField.get(rand)).set(seed);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Random getRand() {
        return rand;
    }
}
