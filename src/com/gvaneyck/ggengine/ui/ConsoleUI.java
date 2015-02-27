package com.gvaneyck.ggengine.ui;

import com.gvaneyck.ggengine.Action;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleUI implements GGui {
    Scanner in = new Scanner(System.in);
    List<Action> currentActions;

    public Action getChoice() {
        int choice = -1;
        while (choice < 0 || choice >= currentActions.size()) {
            choice = in.nextInt();
        }
        return currentActions.get(choice);
    }

    @Override
    public void showChoices(int player, List<Action> actions) {
        System.out.println("Player " + player + "'s turn:");
        currentActions = actions;
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            System.out.print(i + ") ");
            System.out.println(a.toString());
        }
    }

    public void showChoices(List<Action> actions) {

    }

    public void showGS(int player, Map gs) {
        // Swallowed
    }
}
