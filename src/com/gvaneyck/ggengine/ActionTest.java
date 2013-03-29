package com.gvaneyck.ggengine;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.gvaneyck.util.json.JSON;
import com.gvaneyck.util.json.ObjectMap;

public class ActionTest {
	public static void main(String[] args) {
		ActionTest at = new ActionTest();
		at.test();
	}
		
	public void test() {
		StringBuilder scriptText = new StringBuilder();
		File scriptDir = new File("scripts");
		for (File script : scriptDir.listFiles())
			scriptText.append(parseScript(script));
		
		System.out.println(scriptText.toString());

		GroovyShell shell = new GroovyShell();
		Script executor = shell.parse(scriptText.toString());
		
		// Test run all the functions
		for (File script : scriptDir.listFiles()) {
			String func = script.getName();
			if (!func.endsWith(".act"))
				continue;
			func = func.substring(0, func.lastIndexOf("."));
			executor.invokeMethod(func, null);
		}
	}
	
	public String parseScript(File file) {
		if (!file.getName().endsWith(".act"))
			return "";
		
		String func = file.getName();
		func = func.substring(0, func.lastIndexOf("."));
		
		StringBuilder scriptText = new StringBuilder();
		try {
			String fileText = readFile(file);
			ObjectMap data = (ObjectMap)JSON.parse(fileText);
			
			scriptText.append("def " + func + "() {\n");
			scriptText.append(data.getString("code").trim());
			scriptText.append("\n}\n\n");

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
