package com.gvaneyck.ggengine.server;

import lombok.Getter;

@Getter
public class Message {

    private long time;
    private String message;
    private String from; // optional

    public Message(String message) {
        this(System.currentTimeMillis(), message, null);
    }

    public Message(String message, String from) {
        this(System.currentTimeMillis(), message, from);
    }

    public Message(long time, String message, String from) {
        this.time = time;
        this.message = message;
        this.from = from;
    }
}
