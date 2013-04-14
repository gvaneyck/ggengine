package com.gvaneyck.ggengine;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;

import com.gvaneyck.util.Utils;
import com.gvaneyck.util.json.ObjectMap;

/**
 * Represents a rule in the game that can generate actions
 * 
 * @author Gabriel Van Eyck
 */
public class Rule {
	private String name;
	private String code;

	private GroovyShell shell = new GroovyShell();
	private Script script;

	public static Rule loadRule(ObjectMap rule, String name) {
		String code;
		Script script = null;
		if (rule.containsKey("code")) {
			code = rule.getString("code").trim();
			
			try {
			    script = Utils.shell.parse(code);            
			} catch(CompilationFailedException cfe) {
				System.out.println("Failed to parse rule '" + name + "'");
			    System.out.println(cfe.getMessage());
			}
		}
		else {
			code = "";
			System.out.println("Rule '" + name + "' contained no code");
		}

		return new Rule(name, code, script);
	}

	private Rule(String name, String code, Script script) {
		this.name = name;
		this.code = code;
		this.script = script;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}
	
	public String[] getActions(Map<String, Object> gs) {
		shell.setProperty("gs", gs);
		return null;
	}
}
