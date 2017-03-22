package com.gvaneyck.ggengine;

import com.gvaneyck.ggengine.server.GGServer;

public class GGServerTest {
    public static void main(String[] args) {
        try {
            new GGServer(9998).start();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
