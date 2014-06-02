package com.gvaneyck.ggengine;

import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    Scanner in = new Scanner(System.in);
    List<Action> currentActions;
    
    public Action getChoice() {
        int choice = 0;
        while (choice < 0 || choice >= currentActions.size()) {
            choice = in.nextInt();
        }
        return currentActions.get(choice);
    }
    
    public void showChoices(List<Action> actions) {
        currentActions = actions;
        for (int i = 0; i < actions.size(); i++) {
            System.out.println(i);
        }
    }
}
