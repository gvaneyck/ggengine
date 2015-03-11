package com.gvaneyck.ggengine.ui;

import com.gvaneyck.ggengine.Action;

import java.util.List;
import java.util.Scanner;

public class ConsoleUI implements GGui {
    Scanner in = new Scanner(System.in);

    public Action resolveChoice(List<Action> actions) {
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            System.out.print(i + ") ");
            System.out.println(a.toString());
        }

        int choice = -1;
        while (choice < 0 || choice >= actions.size()) {
            choice = in.nextInt();
        }
        return actions.get(choice);
    }
}
