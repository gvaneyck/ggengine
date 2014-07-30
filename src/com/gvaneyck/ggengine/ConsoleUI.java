package com.gvaneyck.ggengine;

import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    Scanner in = new Scanner(System.in);
    List<Action> currentActions;

    public Action getChoice() {
        int choice = -1;
        while (choice < 0 || choice >= currentActions.size()) {
            choice = in.nextInt();
        }
        return currentActions.get(choice);
    }

    public void showChoices(List<Action> actions) {
        currentActions = actions;
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            System.out.print(i + ") ");
            System.out.println(a.toString());
        }
    }
}
