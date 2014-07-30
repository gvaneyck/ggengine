package com.gvaneyck.ggengine;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gvaneyck.ggengine.gui.GGEngine;

public class GameManager2 {
    private Map<String, Object> gs = new HashMap<String, Object>();
    private Map<String, Class> clazzes = new HashMap<String, Class>();
    private Map<String, GroovyObject> instances = new HashMap<String, GroovyObject>();
    private GroovyClassLoader loader = null;
    
    private String source;
    private GGEngine engine;
    private Game game;
    
    private ConsoleUI ui = new ConsoleUI();
    
    public static void main(String[] args) {
//        GameManager2 gm = new GameManager2("tictactoe");
    	GameManager2 gm = new GameManager2("LoveLetter");
    }
    
    public GameManager2(String source) {
        loadClasses(source);
        gameLoop();
    }
    
    private void loadClasses(String source) {
        if (loader == null) {
            ClassLoader parentLoader = this.getClass().getClassLoader();
            loader = new GroovyClassLoader(parentLoader);
            loader.addClasspath(source);
        }
        
        File sourceDir = new File(source);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            System.err.println("Source directory does not exist: " + source);
            return;
        }
        
        File[] sourceFiles = sourceDir.listFiles();
        for (File f : sourceFiles) {
            String fileName = f.getName();
            if (fileName.endsWith(".groovy")) {
                String clazz = fileName.substring(0, fileName.length() - 7);
                loadClass(clazz);
            }
        }

    	if (game == null) {
    		System.err.println("No Game class found, aborting");
    	}
    	
        injectMembers();
    }
    
    private void loadClass(String clazz) {
        try {
            Class groovyClass = loader.loadClass(clazz);
            
            // Bail out if it's abstract or an interface
            int mods = groovyClass.getModifiers();
            if (Modifier.isAbstract(mods) || Modifier.isInterface(mods)) {
            	return;
            }
            
            GroovyObject instance = (GroovyObject)groovyClass.newInstance();
            clazzes.put(clazz, groovyClass);
            instances.put(clazz, instance);
            
            if (instance instanceof Game) {
            	if (game != null) {
            		throw new Exception("Found two Game classes aborting");
            	}
            	game = (Game)instance;
            }
        }
        catch (ClassNotFoundException e) {
            System.err.println("Found file named " + clazz + ".groovy, but it wasn't a class");
        }
        catch (InstantiationException e) {
            System.err.println("Failed to instantiate " + clazz);
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            System.err.println("Illegal access exception for " + clazz);
            e.printStackTrace();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
    private void injectMembers() {
        for (String name : instances.keySet()) {
            GroovyObject instance = instances.get(name);
            Class clazz = clazzes.get(name);
            for (Method m : clazz.getMethods()) {
                String method = m.getName();
                if (!method.startsWith("set"))
                    continue;
                
                String member = method.substring(3);
                if (member.equals("Gs"))
                    instance.invokeMethod("setGs", gs);
                if (member.equals("Gm"))
                    instance.invokeMethod("setGm", this);
                if (clazzes.containsKey(member))
                    instance.invokeMethod(method, instance);
            }
        }
    }
    
    private void executeAction(Action a) {
        int idx = a.clazz.lastIndexOf('.');
        String clazz = a.clazz.substring(0, idx);
        String method = a.clazz.substring(idx + 1);
        
        GroovyObject instance = instances.get(clazz);
        if (instance == null) {
        	System.err.println("Couldn't find instance for " + clazz);
        }
        		
        instance.invokeMethod(method, a.args);
    }
    
    private void gameLoop() {
    	game.init();
    	while (!game.isFinished()) {
    		game.turn();
    	}
    	game.end();
    }
    
    public void presentActions(List<Action> actions) {
    	ui.showChoices(actions);
    	Action action = ui.getChoice();
    	executeAction(action);
    }
    
    public void announce(String message) {
		System.out.println(message);
    }
    
    public void announce(int player, String message) {
		System.err.println(player + ": " + message);
    }
}
