package com.gvaneyck.ggengine.server;

public class GGException extends RuntimeException {

    public GGException(String message) {
        super(message);
    }

    public GGException(String message, Throwable e) {
        super(message, e);
    }
}
