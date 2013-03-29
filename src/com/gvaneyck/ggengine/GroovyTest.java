package com.gvaneyck.ggengine;

import java.io.File;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

public class GroovyTest {
	public static void main(String[] args) {
		GroovyShell shell = new GroovyShell();
		try {
			Script script = shell.parse(new File("scripts/test.groovy"));
			script.invokeMethod("hello_world", null);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
