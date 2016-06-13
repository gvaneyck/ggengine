package com.gvaneyck.ggengine.game;

import com.gvaneyck.ggengine.game.actions.Action;
import com.gvaneyck.ggengine.game.actions.ActionRef;
import com.gvaneyck.ggengine.game.actions.ActionOption;
import com.gvaneyck.ggengine.game.actions.ClosureActionRef;
import com.gvaneyck.ggengine.game.actions.InstanceMethodActionRef;
import com.gvaneyck.ggengine.game.actions.StaticMethodActionRef;
import com.gvaneyck.ggengine.gamestate.GameStateFilter;
import com.gvaneyck.ggengine.gamestate.PublicGSF;
import com.gvaneyck.ggengine.ui.GGui;
import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;

import java.io.File;
import java.lang.reflect.Field;
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
    private Map<String, ActionRef> actions = new HashMap<>();
    private GroovyClassLoader loader = null;

    private Game game;
    private GameStateFilter gsf = new PublicGSF();
    private GGui ui;
    private List<ActionOption> pendingActions = new ArrayList<>();

    private AccessibleRandom internalRand = new AccessibleRandom();
    public Random rand = internalRand.getRand();

    public GameManager(Map<String, Object> initialGameState, GGui ui, String baseDir, String game) {
        this.gs = initialGameState;
        this.ui = ui;
        loadClasses(baseDir, game);
    }

    private void loadClasses(String dir, String pkg) {
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

            Object instance = groovyClass.newInstance();

            if (Game.class.isAssignableFrom(groovyClass)) {
                if (game != null) {
                    throw new Exception("Found two Game classes aborting: " + game.getClass().getName() + " " + groovyClass.getName());
                }
                game = (Game)instance;
            }

            if (GameStateFilter.class.isAssignableFrom(groovyClass)) {
                gsf = (GameStateFilter)instance;
            }

            for (Method method : groovyClass.getMethods()) {
                Action action = method.getAnnotation(Action.class);
                if (action == null) {
                    continue;
                }

                String name = (action.name().isEmpty() ? clazz + "." + method.getName() : action.name());
                if (Modifier.isStatic(method.getModifiers())) {
                    actions.put(name, new StaticMethodActionRef(name, method));
                } else {
                    actions.put(name, new InstanceMethodActionRef(name, instance, method));
                }
            }

            for (Field field : groovyClass.getDeclaredFields()) {
                Action action = field.getAnnotation(Action.class);
                if (action == null) {
                    continue;
                }

                String name = (action.name().isEmpty() ? clazz + "." + field.getName() : action.name());
                field.setAccessible(true);
                Object fieldInstance = field.get(instance);
                if (fieldInstance != null && Closure.class.isAssignableFrom(fieldInstance.getClass())) {
                    actions.put(name, new ClosureActionRef(name, (Closure)fieldInstance));
                }
            }
        }
        catch (ClassNotFoundException e) {
            System.err.println("Found file named " + clazz + ".groovy, but it wasn't a class");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void gameLoop() {
        game.init(this, gs);
        while (!game.isFinished(this, gs)) {
            game.turn(this, gs);
        }
        ui.resolveEnd(game.end(this, gs));
    }

    public void addAction(int player, String name) {
        addAction(player, name, null);
    }

    public void addAction(int player, String name, List<Object> args) {
        ActionRef actionRef = actions.get(name);
        if (actionRef == null) {
            System.err.println("No action found for " + name);
        } else {
            pendingActions.add(new ActionOption(player, actionRef, args));
        }
    }

    public void resolveActions() {
        while (!pendingActions.isEmpty()) {
            ActionOption option = ui.resolveChoice(pendingActions);

            // Remove player's actions from pending in case resolveActions is called again during action resolution
            Iterator<ActionOption> it = pendingActions.iterator();
            while (it.hasNext()) {
                if (it.next().getPlayerId() == option.getPlayerId()) {
                    it.remove();
                }
            }

            option.getActionRef().invoke(this, gs, option.getArgs());
        }
    }

    public void sendMessage(String message) {
        for (int i = 1; i <= (Integer)gs.get("players"); i++) {
            sendMessage(i, message);
        }
    }

    public void sendMessage(int player, String message) {
        ui.sendMessage(player, message);
    }

    public Map getGameState(int player) {
        Map gameState = gsf.filterGameState(gs, player);
        gameState.put("me", player);
        return gameState;
    }
}
