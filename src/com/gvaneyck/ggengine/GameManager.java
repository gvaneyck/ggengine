package com.gvaneyck.ggengine;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFrame;

import com.gvaneyck.ggengine.gui.GGEngine;
import com.gvaneyck.ggengine.gui.Screen;

public class GameManager {
    private Map<String, Object> gs = new HashMap<String, Object>();
    private Map<String, Class> clazzes = new HashMap<String, Class>();
    private Map<String, GroovyObject> instances = new HashMap<String, GroovyObject>();
    private GroovyClassLoader loader = null;
    
    private String source;
    private GGEngine engine;
    
    public static void main(String[] args) {
        GameManager gm = new GameManager("tictactoe");
        gm.start();
    }
    
    public GameManager(String source) {
        buildWindow();
        
        this.source = source;
        loadClasses();
        injectMembers();
    }
    
    public void buildWindow() {
        engine = new GGEngine();
        engine.frame = new JFrame();
        engine.frame.setResizable(false);
        engine.frame.setTitle(engine.TITLE);
        engine.frame.add(engine);
        engine.frame.pack();
        engine.frame.setLocationRelativeTo(null);
        engine.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        engine.frame.setVisible(true);

        engine.start();
    }
    
    public void start() {
        engine.setScreen((Screen)instances.get("TicTacToeView"));
        Scanner in = new Scanner(System.in);
        
        instances.get("TicTacToeRules").invokeMethod("initGame", null);
        while (!(Boolean)instances.get("TicTacToeRules").invokeMethod("isGameEnd", null)) {
            List<Action> actions = (List<Action>)instances.get("TicTacToeRules").invokeMethod("getMoves", null);
            for (int i = 0; i < actions.size(); i++)
                System.out.println(i + ") " + actions.get(i));
            
            int choice = in.nextInt();
            executeAction(actions.get(choice));
            instances.get("TicTacToeActions").invokeMethod("changeTurn", null);
        }
        
        System.out.println("Winner: " + instances.get("TicTacToeRules").invokeMethod("getWinner", null));
        
        instances.get("Test").invokeMethod("printGs", null);
        
        in.close();
    }
    
    private void loadClasses() {
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
    }
    
    private void loadClass(String clazz) {
        try {
            Class groovyClass = loader.loadClass(clazz);
            GroovyObject instance = (GroovyObject)groovyClass.newInstance();
            clazzes.put(clazz, groovyClass);
            instances.put(clazz, instance);
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
                if (clazzes.containsKey(member))
                    instance.invokeMethod(method, instance);
            }
        }
    }
    
    private void executeAction(Action a) {
        int idx = a.clazz.lastIndexOf('.');
        String clazz = a.clazz.substring(0, idx);
        String method = a.clazz.substring(idx + 1);
        instances.get(clazz).invokeMethod(method, a.args);
    }
    
    private String readFile(String file) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String line;
        
        StringBuilder buff = new StringBuilder();
        while ((line = in.readLine()) != null)
            buff.append(line + "\n");
        
        in.close();
        return buff.toString();
    }
}
