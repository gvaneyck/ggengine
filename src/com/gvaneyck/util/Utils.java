package com.gvaneyck.util;

import groovy.lang.GroovyShell;

import java.util.regex.Pattern;

public class Utils {
    public static final GroovyShell shell = new GroovyShell();

    public static String indent(String data) {
        return Pattern.compile("^", Pattern.MULTILINE).matcher(data.trim()).replaceAll("    ");
    }
}
