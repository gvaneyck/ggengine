package com.gvaneyck.ggengine;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.util.HashMap;
import java.util.Map;

public class GameTest {
	public static Map<String, Object> gs = new HashMap<String, Object>();

	public static void main(String[] args) {
		try {
			ResourceLoader resources = new ResourceLoader();
			resources.loadActions("tictactoe");
			resources.compileResources("classes2");
			
			Map<String, Map<String, Action>> actions = resources.getActions();
			ClassLoader parent = (new ActionTest()).getClass().getClassLoader();
			GroovyClassLoader loader = new GroovyClassLoader(parent);
			loader.addClasspath("classes2");
			
			// Set up Util class
			Class utilClass = loader.loadClass("Util");
			GroovyObject utilObj = (GroovyObject)utilClass.newInstance();
			utilObj.setProperty("gs", gs);

			Map<String, Class> classMap = new HashMap<String, Class>();
			for (String clazz : actions.keySet()) {
				Class groovyClass = loader.loadClass(clazz);
				classMap.put(clazz, groovyClass);
			}
			gs.put("classes", classMap);
			
			runGame();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void runGame() {
		while (true) {
			
		}
	}
}
