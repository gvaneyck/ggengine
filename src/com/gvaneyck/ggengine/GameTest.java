package com.gvaneyck.ggengine;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class GameTest {
	public static Map<String, Object> gs = new HashMap<String, Object>();
	public static Map<String, Class> classMap = new HashMap<String, Class>();

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
	
	public static void runGame() throws Exception {
		Scanner in = new Scanner(System.in);
		GroovyObject obj = (GroovyObject)classMap.get("Rules").newInstance();
		obj.setProperty("gs", gs);
		obj.invokeMethod("init", null);
		
		while (true) {
			printGameState();
			checkEnd();
			
			List<Map<String, Object>> options = new ArrayList<Map<String, Object>>();
			
			Object result = obj.invokeMethod("getActions", null);
			if (result instanceof List) {
				options.addAll((List)result);
			}
			else if (result instanceof String) {
				Map<String, Object> option = new HashMap<String, Object>();
				option.put("action", result);
				options.add(option);
			}
			else {
				System.out.println("Invalid actions returned from rule:");
				System.out.println(result);
				continue;
			}
			
			for (int i = 0; i < options.size(); i++) {
				Map<String, Object> option = options.get(i);
				System.out.print(i + ") " + option.get("action"));
				if (option.containsKey("args")) {
					System.out.print(", " + option.get("args"));
				}
				System.out.println();
			}
			
			Map<String, Object> choice = options.get(in.nextInt());  
			String action = (String)choice.get("action");
			Object args = choice.get("args");
			int idx = action.indexOf('.');
			
			GroovyObject obj2 = (GroovyObject)classMap.get(action.substring(0, idx)).newInstance();
			obj2.setProperty("gs", gs);
			obj2.invokeMethod(action.substring(idx + 1), args);

			if (gs.get("activePlayer").equals("X"))
				gs.put("activePlayer", "O");
			else
				gs.put("activePlayer", "X");
		}
	}
	
	public static void printGameState() {
		System.out.println("Current player: " + gs.get("activePlayer"));
		String[][] board = (String[][])gs.get("board");
		StringBuilder buff = new StringBuilder();
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (board[x][y] != null)
					buff.append(board[x][y]);
				else
					buff.append(' ');
				if (y < 2)
					buff.append('|');
			}
			buff.append('\n');
			if (x < 2)
				buff.append("-+-+-\n");
		}
		System.out.println(buff.toString());
	}
	
	public static void checkEnd() throws Exception {
		GroovyObject obj = (GroovyObject)classMap.get("Rules").newInstance();
		obj.setProperty("gs", gs);
		boolean result = Boolean.valueOf((Boolean)obj.invokeMethod("checkEnd", null));
		if (result) {
			System.out.println("We have a winner!");
			System.exit(0);
		}
	}
}
