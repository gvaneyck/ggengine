package com.gvaneyck.ggengine.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gvaneyck.ggengine.game.actions.ActionOption;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleUI implements GGui {

    private Scanner in = new Scanner(System.in);
    private ObjectMapper objectMapper = new ObjectMapper();

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
        try {
            System.out.println(objectMapper.writeValueAsString(data));
        } catch (Exception e) {

        }
    }
}
