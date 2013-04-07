package com.gvaneyck.ggengine;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionTest {
	public static void main(String[] args) {
		ResourceLoader resources = new ResourceLoader();
		resources.loadResources();
		List<Action> actions = resources.getActions();
		
		StringBuilder scriptText = new StringBuilder();
		for (Action action : actions) {
			scriptText.append(action.getDefinition());
			scriptText.append("\n");
		}
		
		System.out.println(scriptText.toString());

		Map<String, Object> gs = new HashMap<String, Object>();
		Binding binding = new Binding();
		binding.setVariable("gs", gs);
		
		GroovyShell shell = new GroovyShell(binding);
		Script executor = shell.parse(scriptText.toString());
		
		// Test run all the functions
		for (Action action : actions) {
			System.out.println("--------");
			System.out.println(action.getName() + ":");
			executor.invokeMethod(action.getName(), null);
		}
		
		// Test the game state
		System.out.println(gs);
	}
	
	
}
