package com.gvaneyck.ggengine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gvaneyck.util.FileUtils;
import com.gvaneyck.util.Utils;
import com.gvaneyck.util.json.JSON;
import com.gvaneyck.util.json.JSONParsingException;
import com.gvaneyck.util.json.ObjectMap;

public class ResourceLoader {
	private String actionDir = "actions";
	private String classDir = "classes";
	
	private Map<String, Map<String, Action>> actions;
	
	public ResourceLoader() {
		
	}
	
	public ResourceLoader(ObjectMap config) {
		if (config.containsKey("actionDir")) {
			actionDir = config.getString("actionDir");
		}
	}
	
	public Map<String, Map<String, Action>> getActions() {
		return actions;
	}
	
	public void loadResources() {
		actions = loadActionDir(new File(actionDir));
	}
	
	public void compileResources() {
		for (String clazz : actions.keySet()) {
			StringBuilder clazzData = new StringBuilder();
			
			clazzData.append("class ");
			clazzData.append(clazz);
			clazzData.append(" {\n");
			
			clazzData.append("\tstatic gs\n");
			clazzData.append("\n");
			
			for (Action action : actions.get(clazz).values()) {
				String actionData = action.getDefinition();
				actionData = Utils.indent(actionData);
				clazzData.append(actionData);
				clazzData.append("\n");
			}
			
			clazzData.append("}\n");
			
			exportClass(clazz, clazzData.toString());
		}
	}
	
	private void exportClass(String clazz, String data) {
		System.out.println(data);
		
		// Make the output folder if it doesn't exist
		(new File(classDir)).mkdir();
		
		try {
			String fileName = String.format("%s/%s.groovy", classDir, clazz);
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName), data.length() + 1);
			out.write(data);
			out.close();
		}
		catch (IOException e) {
			System.out.println("Failed to generate class for " + clazz);
			e.printStackTrace();
		}
	}
	
	private Map<String, Map<String, Action>> loadActionDir(File dir) {
		Map<String, Map<String, Action>> result = new HashMap<>();
		if (!dir.isDirectory()) {
			System.out.println("No action directory found: " + dir.getPath());
			return result;
		}
		
		for (File file : dir.listFiles()) {
			if (!file.isDirectory())
				continue;
			
			String clazz = file.getName();
			Map<String, Action> actions = loadActions(file);
			result.put(clazz, actions);
		}
		
		return result;
	}
	
	private Map<String, Action> loadActions(File dir) {
		Map<String, Action> actions = new HashMap<String, Action>();
		
		for (File file : dir.listFiles()) {
			if (!file.isFile() || !file.getName().endsWith(".act"))
				continue;
			
			try {
				ObjectMap data = (ObjectMap)JSON.parse(FileUtils.readFile(file));
				String name = file.getName().substring(0, file.getName().length() - 4);
				actions.put(name, Action.loadAction(data, name));				
			}
			catch (IOException e) {
				System.out.println("Error when reading " + file.getName());
				e.printStackTrace();
			}
			catch (JSONParsingException e) {
				System.out.println("Failed to parse JSON in " + file.getName());
				e.printStackTrace();
			}
		}
		
		return actions;
	}
}
