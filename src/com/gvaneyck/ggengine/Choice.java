package com.gvaneyck.ggengine;

import java.util.Map;

public class Choice {
	public String action;
	public Object args;
	
	public static Choice getChoice(Object obj) {
		if (obj instanceof String) {
			return new Choice((String)obj);
		}
		else if (obj instanceof Map<?,?>) {
			return new Choice((Map<String, Object>)obj);
		}
		else {
			System.out.println("Unrecognized choice");
			return null;
		}
	}
	
	public Choice(String action) {
		this.action = action;
	}
	
	public Choice(String action, Object args) {
		this.action = action;
		this.args = args;
	}
	
	public Choice(Map<String, Object> choice) {
		this.action = choice.get("action").toString();
		this.args = choice.get("args");
	}
}
