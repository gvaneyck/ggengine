package com.gvaneyck.ggengine;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.gvaneyck.util.json.JSON;
import com.gvaneyck.util.json.ObjectMap;

public class ActionTest {
	public static void main(String[] args) {
		ActionTest at = new ActionTest();
		at.test();
	}
	
	public ArrayList<String> funcs = new ArrayList<String>();
		
	public void test() {
		String scriptText = loadDir(new File("scripts"), "");
		
		System.out.println(scriptText);

		Map<String, Object> gs = new HashMap<String, Object>();
		Binding binding = new Binding();
		binding.setVariable("gs", gs);
		GroovyShell shell = new GroovyShell(binding);
		Script executor = shell.parse(scriptText);
		
		// Test run all the functions
		for (String func : funcs) {
			System.out.println("--------");
			System.out.println(func + ":");
			executor.invokeMethod(func, null);
			System.out.println("--------");
		}
		
		// Test the game state
		System.out.println(gs);
	}
	
	public String loadDir(File dir, String clazz) {
		StringBuilder scriptText = new StringBuilder();
		
		for (File script : dir.listFiles()) {
			if (script.isDirectory()) {
				String subClazz = clazz + "_" + script.getName();
				scriptText.append("class " + subClazz + " {\n");
				scriptText.append(loadDir(script, subClazz));
				scriptText.append("}\n");
			}
			else if (script.isFile() && script.getName().endsWith(".act")) {
				scriptText.append(parseScript(script, !clazz.isEmpty()));				
			}
		}
		
		return scriptText.toString();
	}
	
	public String parseScript(File file, boolean inClass) {
		String func = file.getName();
		func = func.substring(0, func.lastIndexOf("."));
		
		StringBuilder scriptText = new StringBuilder();
		try {
			String fileText = readFile(file);
			ObjectMap data = (ObjectMap)JSON.parse(fileText);
			
			String code = data.getString("code").trim();
			code =  Pattern.compile("^", Pattern.MULTILINE).matcher(code).replaceAll("\t");
			if (!inClass) {
				scriptText.append("def " + func + "() {\n");
				scriptText.append(code + "\n");
				scriptText.append("}\n\n");
				funcs.add(func);
			}
			else {
				scriptText.append("\tstatic " + func + "() {\n");
				scriptText.append("\t" + code + "\n");
				scriptText.append("\t}\n\n");
			}
		}
		catch (Exception e) {
			System.out.println("Failed to parse script named '" + file.getName() + "'");
			e.printStackTrace();
		}
		
		return scriptText.toString();
	}
	
	private String readFile(File file) throws IOException {
		FileReader in = new FileReader(file);
		StringBuilder buff = new StringBuilder();
		int c;
		while ((c = in.read()) != -1)
			buff.append((char)c);
		in.close();
		
		return buff.toString();
	}
}
