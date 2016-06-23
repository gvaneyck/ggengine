package com.gvaneyck.ggengine.game;

import com.gvaneyck.ggengine.game.actions.Action;
import com.gvaneyck.ggengine.game.actions.ActionRef;
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
import java.util.LinkedHashMap;
import java.util.Map;

public class GameManager {

    private static Map<String, GameManager> instances = new LinkedHashMap<>();

    private Map<String, ActionRef> actions = new LinkedHashMap<>();
    private GroovyClassLoader loader = null;

    private Class gameClass;
    private Class gsfClass = PublicGSF.class;

    private GameManager(String baseDir, String game) {
        loadClasses(baseDir, game);
    }

    // Package private
    protected static GameManager getInstance(String baseDir, String game) {
        String key = baseDir + "/" + game;
        if (!instances.containsKey(key)) {
            instances.put(key, new GameManager(baseDir, game));
        }
        return instances.get(key);
    }

    public Class getGameClass() {
        return gameClass;
    }

    public Class getGsfClass() {
        return gsfClass;
    }

    public Map<String, ActionRef> getActions() {
        return actions;
    }

    private void loadClasses(String dir, String pkg) {
        if (loader == null) {
            ClassLoader parentLoader = this.getClass().getClassLoader();
            loader = new GroovyClassLoader(parentLoader);
            loader.addClasspath(dir);
        }

        String sourcePath = dir + "/" + pkg;
        File sourceDir = new File(sourcePath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            System.err.println("Warning - Source directory does not exist: " + sourcePath);
            return;
        }

        loadClasses(pkg + ".", sourceDir);

        if (gameClass == null) {
            System.err.println("Warning - No game class found: " + sourcePath);
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
                if (gameClass != null) {
                    throw new Exception("Found two Game classes aborting: " + gameClass.getName() + " " + groovyClass.getName());
                }
                gameClass = groovyClass;
            }

            if (GameStateFilter.class.isAssignableFrom(groovyClass)) {
                gsfClass = groovyClass;
            }

            for (Method method : groovyClass.getMethods()) {
                Action action = method.getAnnotation(Action.class);
                if (action == null) {
                    continue;
                }

                String name = (action.value().isEmpty() ? clazz + "." + method.getName() : action.value());
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

                String name = (action.value().isEmpty() ? clazz + "." + field.getName() : action.value());
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
}
