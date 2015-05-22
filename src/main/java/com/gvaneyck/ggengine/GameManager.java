package com.gvaneyck.ggengine;

import com.gvaneyck.ggengine.gamestate.GameStateFilter;
import com.gvaneyck.ggengine.ui.ConsoleUI;
import com.gvaneyck.ggengine.ui.GGui;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameManager {
    private Map<String, Object> gs;
    private Map<String, Class> clazzes = new HashMap<String, Class>();
    private GroovyClassLoader loader = null;
    private GameStateFilter gsf = new GameStateFilter();

    private Game game;

    private GGui ui;

    private AccessibleRandom internalRand = new AccessibleRandom();
    public Random rand = internalRand.getRand();

    public GameManager() {
        this(new HashMap<String, Object>(), new ConsoleUI());
    }

    public GameManager(Map<String, Object> gs) {
        this(gs, new ConsoleUI());
    }

    public GameManager(Map<String, Object> gs, GGui ui) {
        this.gs = gs;
        this.ui = ui;
    }

    public void loadGame(String baseDir, String game) {
        loadClasses(baseDir + "/" + game, game);
    }

    public void loadClasses(String dir, String pkg) {
        if (loader == null) {
            ClassLoader parentLoader = this.getClass().getClassLoader();
            loader = new GroovyClassLoader(parentLoader);
            loader.addClasspath(dir);
        }

        File sourceDir = new File(dir);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            System.err.println("Source directory does not exist: " + dir);
            return;
        }

        loadClasses(pkg + ".", sourceDir);

        if (game == null) {
            System.err.println("No Game class found, aborting");
            System.exit(0);
        }

        injectMembers();
    }

    private void loadClasses(String pkg, File dir) {
        File[] sourceFiles = dir.listFiles();
        for (File f : sourceFiles) {
            String fileName = f.getName();
            if (f.isDirectory()) {
                loadClasses(pkg + fileName + ".", f);
            }
            else {
                if (fileName.endsWith(".groovy")) {
                    String clazz = fileName.substring(0, fileName.length() - 7);
                    loadClass(pkg, clazz);
                }
            }
        }
    }

    private void loadClass(String pkg, String clazz) {
        try {
            String fullName = pkg + clazz;
            Class groovyClass = loader.loadClass(fullName);

            if (!groovyClass.getName().equals(fullName)) {
                throw new Exception("File not named appropriately for class: " + groovyClass.getName() + " (class name) vs " + fullName + " (file name)");
            }

            if (Game.class.isAssignableFrom(groovyClass)) {
                if (game != null) {
                    throw new Exception("Found two Game classes aborting: " + game.getClass().getName() + " " + groovyClass.getName());
                }
                game = (Game)groovyClass.newInstance();
            }

            if (GameStateFilter.class.isAssignableFrom(groovyClass)) {
                gsf = (GameStateFilter)groovyClass.newInstance();
            }

            clazzes.put(clazz, groovyClass);
        }
        catch (ClassNotFoundException e) {
            System.err.println("Found file named " + clazz + ".groovy, but it wasn't a class");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void injectMembers() {
        for (String name : clazzes.keySet()) {
            Class clazz = clazzes.get(name);
            try {
                for (Method m : clazz.getMethods()) {
                    String method = m.getName();
                    if (!method.startsWith("set") || !Modifier.isStatic(m.getModifiers())) {
                        continue;
                    }

                    String member = method.substring(3);
                    if (member.equals("Gs")) {
                        m.invoke(null, gs);
                    }
                    if (member.equals("Gm")) {
                        m.invoke(null, this);
                    }
                }
            }
            catch (Exception e) {
                System.err.println("Error when injecting for " + clazz.getName());
                e.printStackTrace();
            }
        }
    }

    public void gameLoop() {
        game.init();
        while (!game.isFinished()) {
            game.turn();
        }
        ui.resolveEnd(game.end());
    }

    public void presentActions(List<Action> actions) throws Exception {
        if (actions.isEmpty()) {
            throw new Exception("No actions to choose from");
        }
        else if (actions.size() == 1) {
            actions.get(0).invoke();
        }
        else {
//            getGameState(1);
            Action action = ui.resolveChoice(actions);
            action.invoke();
        }
    }

    public Map getGameState(int player) {
        return gsf.filterGameState(gs, player);
    }
}
