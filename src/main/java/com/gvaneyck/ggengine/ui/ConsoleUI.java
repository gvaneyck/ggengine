package com.gvaneyck.ggengine.ui;

import com.gvaneyck.ggengine.game.actions.ActionOption;
import com.gvaneyck.ggengine.server.util.JSON;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleUI implements GGui {

    private Scanner in = new Scanner(System.in);

    public void sendMessage(int player, String message) {
        System.out.println("@" + player + " - " + message);
    }

    public ActionOption resolveChoice(List<ActionOption> actions) {
        for (int i = 0; i < actions.size(); i++) {
            System.out.println(i + ") " + actions.get(i).toString());
        }
        int choice = in.nextInt();
        return actions.get(choice);
    }

    public void resolveEnd(Map data) {
        System.out.println(JSON.writeValueAsString(data));
    }
}
