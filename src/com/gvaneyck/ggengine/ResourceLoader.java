package com.gvaneyck.ggengine;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.gvaneyck.util.FileUtils;
import com.gvaneyck.util.json.JSON;
import com.gvaneyck.util.json.JSONParsingException;
import com.gvaneyck.util.json.ObjectMap;

public class ResourceLoader {
	private String actionDir = "actions";
	
	private Map<String, Action> actions;
	
	public ResourceLoader() {
		
	}
	
	public ResourceLoader(ObjectMap config) {
		if (config.containsKey("actionDir")) {
			actionDir = config.getString("actionDir");
		}
	}
	
	public Map<String, Action> getActions() {
		return actions;
	}
	
	public void loadResources() {
		actions = loadActions(new File(actionDir));
	}
	
	private Map<String, Action> loadActions(File dir) {
		return loadActions(dir, "", true);
	}
	
	private Map<String, Action> loadActions(File dir, String prefix, boolean recurse) {
		Map<String, Action> actions = new HashMap<String, Action>();
		
		for (File file : dir.listFiles()) {
			if (file.isDirectory() && recurse) {
				String namePrefix = file.getName() + "_";
				actions.putAll(loadActions(file, namePrefix, false));
			}
			else if (file.isFile() && file.getName().endsWith(".act")) {
				try {
					ObjectMap data = (ObjectMap)JSON.parse(FileUtils.readFile(file));
					String name = prefix + file.getName().substring(0, file.getName().length() - 4);
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
		}
		
		return actions;
	}
}
