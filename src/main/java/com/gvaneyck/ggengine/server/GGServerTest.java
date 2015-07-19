package com.gvaneyck.ggengine.server;

public class GGServerTest {
    public static void main(String[] args) {
        try {
            new GGServer().start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
