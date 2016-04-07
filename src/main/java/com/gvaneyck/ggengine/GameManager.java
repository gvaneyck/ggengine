package com.gvaneyck.ggengine;

import com.gvaneyck.ggengine.gamestate.GameStateFilter;
import com.gvaneyck.ggengine.gamestate.PublicGSF;
import com.gvaneyck.ggengine.ui.ConsoleUI;
import com.gvaneyck.ggengine.ui.GGui;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameManager {
    private Map<String, Object> gs;
    private Map<String, Class> clazzes = new HashMap<String, Class>();
    private GroovyClassLoader loader = null;
    private GameStateFilter gsf = new PublicGSF();

    private Game game;
    private GGui ui;
    private List<Action> pendingActions = new ArrayList<Action>();

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
        loadClasses(baseDir, game);
    }

    public void loadClasses(String dir, String pkg) {
        if (loader == null) {
            ClassLoader parentLoader = this.getClass().getClassLoader();
            loader = new GroovyClassLoader(parentLoader);
            loader.addClasspath(dir);
        }

        File sourceDir = new File(dir + "/" + pkg);
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
                System.err.println("Error during injection for " + clazz.getName());
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

    public void addAction(Action action) {
        pendingActions.add(action);
    }

    public void addActions(List<Action> actions) {
        pendingActions.addAll(actions);
    }

    public void resolveActions() {
        while (!pendingActions.isEmpty()) {
            Action action = ui.resolveChoice(pendingActions);

            // Remove player's actions from pending in case resolveActions is called during action resolution
            // TODO: Unroll recursion
            Iterator<Action> it = pendingActions.iterator();
            while (it.hasNext()) {
                if (it.next().getPlayerId() == action.getPlayerId()) {
                    it.remove();
                }
            }

            action.invoke();
        }
    }

    public Map getGameState(int player) {
        Map gameState = gsf.filterGameState(gs, player);
        gameState.put("me", player);
        return gameState;
    }
}
