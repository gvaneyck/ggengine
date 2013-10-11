package com.gvaneyck.ggengine;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class GameTest {
    public static Map<String, Object> gs = new HashMap<String, Object>();
    public static Map<String, Class> clazzes = new HashMap<String, Class>();
    public static Map<String, GroovyObject> instances = new HashMap<String, GroovyObject>();
    public static GroovyClassLoader loader = null;
    
    public static void main(String[] args) {
        try {
            ClassLoader parent = (new GameTest()).getClass().getClassLoader();
            loader = new GroovyClassLoader(parent);
            loader.addClasspath("tictactoe");
            
            loadClass("TicTacToe");
            injectMembers();
            instances.get("TicTacToe").invokeMethod("print", null);
            
            loader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void loadClass(String clazz) throws Exception{
        Class groovyClass = loader.loadClass(clazz);
        GroovyObject instance = (GroovyObject)groovyClass.newInstance();
        clazzes.put(clazz, groovyClass);
        instances.put(clazz, instance);
    }
    
    public static void injectMembers() {
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
    
    public static String readFile(String file) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
        String line;
        
        StringBuilder buff = new StringBuilder();
        while ((line = in.readLine()) != null)
            buff.append(line + "\n");
        
        in.close();
        return buff.toString();
    }
}
