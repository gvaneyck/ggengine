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
	public static Map<String, GroovyObject> objectMap = new HashMap<String, GroovyObject>();
	

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
			
			List<Choice> choices = getChoices();
			printChoices(choices);
			
			Choice choice = choices.get(in.nextInt());  
			invokeAction(choice.action, choice.args);

			if (gs.get("activePlayer").equals("X"))
				gs.put("activePlayer", "O");
			else
				gs.put("activePlayer", "X");
		}
	}
	
	public static void printChoices(List<Choice> choices) {
		for (int i = 0; i < choices.size(); i++) {
			Choice choice = choices.get(i);
			System.out.print(i + ") " + choice.action);
			if (choice.args != null) {
				System.out.print(", " + choice.args);
			}
			System.out.println();
		}
	}
	
	public static List<Choice> getChoices() throws InstantiationException, IllegalAccessException {
		List<Choice> choices = new ArrayList<Choice>();
		
		GroovyObject obj = getObject("Rules");
		Object result = obj.invokeMethod("getActions", null);

		if (result instanceof List) {
			List<?> resultList = (List<?>)result;
			for (Object item : resultList)
				choices.add(Choice.getChoice(item));
		}
		else {
			choices.add(Choice.getChoice(result));
		}
		
		return choices;
	}
	
	public static void invokeAction(String action, Object args) throws InstantiationException, IllegalAccessException {
		int idx = action.indexOf('.');
		String clazzName = action.substring(0, idx);
		String methodName = action.substring(idx + 1);
		
		GroovyObject obj = getObject(clazzName);
		obj.invokeMethod(methodName, args);
	}
	
	public static GroovyObject getObject(String clazzName) throws InstantiationException, IllegalAccessException {
		if (!objectMap.containsKey(clazzName)) {
			GroovyObject obj = (GroovyObject)classMap.get(clazzName).newInstance();
			obj.setProperty("gs", gs);
			objectMap.put(clazzName, obj);
		}
		
		return objectMap.get(clazzName);
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
