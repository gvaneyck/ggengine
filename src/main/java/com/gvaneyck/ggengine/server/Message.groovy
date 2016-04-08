package com.gvaneyck.ggengine.server

public class Message {
    long time
    String message
    String from // optional

    public Message(String message) {
        this(System.currentTimeMillis(), message, null)
    }

    public Message(String message, String from) {
        this(System.currentTimeMillis(), message, from)
    }

    public Message(long time, String message, String from) {
        this.time = time
        this.message = message
        this.from = from
    }
}
