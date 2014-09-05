package com.gvaneyck.ggengine.fe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class IRCConnector {

    // State variables
    private boolean running;
    private boolean reconnect;
    private boolean joined = false;
    private boolean retrievenick;
    private long lastPing = -1;
    private int wait_for_pong = 0;

    // Connection variables
    private ArrayList<String> servers;
    private String server;
    private int serverIdx;
    private int port;
    private String userName;
    private String password;
    private Map<String, String> channels;

    private Socket socket = null;
    private BufferedReader in = null;
    private BufferedWriter out = null;

    // Buffer variables
    private List<String> buffer = Collections.synchronizedList(new LinkedList<String>());

    // Settings
    private static final long SLEEP_TIME = 100;
    private static final long PING_RATE = 15000;

    // RNG
    Random rand = new Random();

    public IRCConnector(String[] servers, int port) {
        this.servers = new ArrayList<String>();
        for (String s : servers) {
            this.servers.add(s);
        }
        this.serverIdx = 0;

        this.port = port;
    }

    public void setUser(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (Exception e) {
        }
    }

    public void run() {
        running = true;
        while (running) {
            try {
                // Need to open ourself?
                if (socket == null) {
                    open();
                }

                String line;
                boolean finished;

                // Read loop & reconnect if necessary
                reconnect = false;
                retrievenick = false;
                while (running && !reconnect) {
                    // Read attempt loop
                    // Send pings every PING_RATE of non-activity
                    finished = false;
                    line = "";
                    while (true) {
                        // Ping server if necessary
                        if (System.currentTimeMillis() - lastPing > PING_RATE) {
                            // Check if we're still waiting for a PONG (i.e. we timed out)
                            if (wait_for_pong == 2) {
                                // Easy way to break
                                throw new Exception();
                            }

                            write("PING " + serverIdx);
                            lastPing = System.currentTimeMillis();
                            wait_for_pong++;
                        }

                        // Grab input if any
                        while (in.ready()) {
                            char c = (char) in.read();

                            // break if newline
                            if (c == '\n') {
                                finished = true;
                                break;
                            }

                            // Don't add carriage returns
                            if (c != '\r') {
                                line += c;
                            }
                        }

                        if (finished) {
                            break;
                        }

                        sleep(100);
                    }

                    // Reset time for necessary PING (since we got data)
                    lastPing = System.currentTimeMillis();

                    // Respond to pings
                    if (line.startsWith("PING")) {
                        write("PONG " + line.substring(5));
                    }

                    // Watch for pongs
                    if (line.contains("PONG")) {
                        wait_for_pong = 0;
                        continue;
                    }

                    // Join on MODE
                    if (!joined && line.contains("MODE")) {
                        // Auth on mode too :(
                        if (server.contains("quakenet")) {
                            write("PRIVMSG Q@CServe.quakenet.org :AUTH " + userName.replaceAll("[_|]", "") + " " + password + "1");
                        }

                        sleep(1000);

                        for (String channel : channels.keySet()) {
                            write("JOIN " + channel + " " + channels.get(channel));
                            System.out.println("Joining " + channel + " on " + server);
                        }

                        joined = true;
                    }

                    // (Re)Identify if needed
                    if (line.startsWith(":NickServ") && line.contains("NOTICE " + userName + " :This nickname is registered and protected.")) {
                        sendMessage("NickServ", "IDENTIFY " + password);
                    }

                    // Get name back if needed
                    if (line.contains("433") && line.contains(":Nickname is already in use.")) {
                        write("NICK " + userName + rand.nextInt(100));

                        // Don't use NickServ on quakenet and instead hope that the name times out
                        if (server.contains("quakenet")) {
                            retrievenick = true;
                        }
                        // GHOST on non-quakenet servers
                        else {
                            sleep(1000);
                            sendMessage("NickServ", "GHOST " + userName + " " + password);
                            sleep(1000);
                            write("NICK " + userName);
                        }
                    }

                    if (retrievenick && line.startsWith(":" + userName + "!") && line.contains("QUIT")) {
                        write("NICK " + userName);
                        retrievenick = false;
                    }

                    buffer.add(line);
                }
            }
            catch (Exception e) {
                System.out.println("DC from " + server);
                e.printStackTrace(System.out);
            }

            close();

            buffer.clear();

            // Wait 3s (to avoid reconnect spam)
            sleep(3000);
        }
    }

    public void die() {
        running = false;
    }

    public void quit(String msg) {
        for (String channel : channels.keySet()) {
            write("PART " + channel + " :" + msg);
        }
        write("QUIT");
    }

    private void open() throws IOException {
        server = servers.get(serverIdx);
        serverIdx++;
        if (serverIdx == servers.size()) {
            serverIdx = 0;
        }
        System.out.println("Connecting to " + server);

        socket = new Socket(server, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

        // Set up username
        String shortName = userName.toLowerCase().replaceAll("[^a-z]", "");
        write("USER " + shortName + " \"\" \"\" :" + shortName);
        write("NICK " + userName);

        lastPing = System.currentTimeMillis();
        wait_for_pong = 0;
    }

    private void close() {
        try {
            in.close();
        }
        catch (Exception e) {
        }
        try {
            out.close();
        }
        catch (Exception e) {
        }
        try {
            socket.close();
        }
        catch (Exception e) {
        }

        in = null;
        out = null;
        socket = null;

        joined = false;
    }

    public String readLine() {
        if (buffer.isEmpty()) {
            return null;
        }
        else {
            return buffer.remove(0);
        }
    }

    public void write(String data) {
        try {
            out.write(data + "\n");
            out.flush();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String msg) {
        for (String channel : channels.keySet()) {
            sendMessage(channel, msg);
        }
    }

    public void sendMessage(String target, String msg) {
        sendMsg(target, msg, false);
    }

    public void sendNotice(String target, String msg) {
        sendMsg(target, msg, true);
    }

    private synchronized void sendMsg(String target, String msg, boolean isNotice) {
        if (target == null || target.length() == 0 || msg == null || msg.length() == 0) {
            return;
        }

        if (out == null) {
            System.out.println("Error, tried sending message before connection was set up -> " + target + ": " + msg);
        }

        try {
            String[] msgLines = msg.split("\n");

            // Send a message for every line
            for (String line : msgLines) {
                if (line.length() == 0) {
                    continue;
                }

                // Split messages that are too long into multiple lines
                int i = 0;
                line += " ";
                do {
                    int idx = line.lastIndexOf(" ", i + 435);

                    StringBuffer ret = new StringBuffer();
                    if (isNotice) {
                        ret.append("NOTICE ");
                    }
                    else {
                        ret.append("PRIVMSG ");
                    }

                    ret.append(target);
                    ret.append(" :");
                    ret.append(line.substring(i, idx));
                    System.out.println(server + " " + ret.toString());
                    ret.append("\n");
                    write(ret.toString());

                    sleep(SLEEP_TIME);

                    i = idx + 1;
                } while (i < line.length());
            }

            // I think sending messages means we don't get pings, so reset ping time
            lastPing = System.currentTimeMillis();
        }
        catch (Exception e) {
            e.printStackTrace();
            reconnect = true; // Forces a reconnect
        }
    }
}
