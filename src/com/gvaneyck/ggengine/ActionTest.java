package com.gvaneyck.ggengine;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.util.HashMap;
import java.util.Map;

public class ActionTest {
	public static final Map<String, Object> gs = new HashMap<String, Object>();
	
	public static void main(String[] args) {
		try {
			ResourceLoader resources = new ResourceLoader();
			resources.loadResources();
			resources.compileResources();
			
			Map<String, Map<String, Action>> actions = resources.getActions();
			ClassLoader parent = (new ActionTest()).getClass().getClassLoader();
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			loader.addClasspath("classes");
			
			for (String clazz : actions.keySet()) {
				Class groovyClass = loader.loadClass(clazz);
				
				GroovyObject obj = (GroovyObject)groovyClass.newInstance();
				obj.setProperty("gs", gs);
				
				for (Action action : actions.get(clazz).values()) {
					String[] args2 = null;
					if (action.getArgs().length > 0)
						args2 = action.getArgs();
					
					System.out.println("--------");
					System.out.println("gs = " + gs);
					System.out.println("Invoking " + clazz + "." + action.getName() + ":");
					
					obj.invokeMethod(action.getName(), args2);
					
					// Check if the gamestate was reassigned
					if (gs != obj.getProperty("gs")) {
						System.out.println(clazz + "." + action.getName() + " changed the gamestate reference!");
						obj.setProperty("gs", gs);
					}
					System.out.println("gs = " + gs);
				}
			}
			
			loader.close();
		}
		catch (Exception e) {
			System.out.println("Error during test");
			e.printStackTrace();
		}
	}
}
