package com.gvaneyck.ggengine.game.util;

import com.gvaneyck.ggengine.server.GGException;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class AccessibleRandom {

    private static Field seedField;

    static {
        try {
            seedField = Random.class.getDeclaredField("seed");
            seedField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Getter private Random rand = new Random();

    public long getSeed() {
        long seed = 0;
        try {
            seed = ((AtomicLong)seedField.get(rand)).get();
        } catch (Exception e) {
            throw new GGException("Error accessing seed", e);
        }
        return seed;
    }

    public void setSeed(long seed) {
        try {
            ((AtomicLong)seedField.get(rand)).set(seed);
        } catch (Exception e) {
            throw new GGException("Error setting seed", e);
        }
    }
}
