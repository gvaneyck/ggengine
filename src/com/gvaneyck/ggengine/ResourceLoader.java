package com.gvaneyck.ggengine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.gvaneyck.util.FileUtils;
import com.gvaneyck.util.Utils;
import com.gvaneyck.util.encoding.ObjectMap;
import com.gvaneyck.util.encoding.json.JSON;
import com.gvaneyck.util.encoding.json.JSONParsingException;

public class ResourceLoader {
	
	private Map<String, Map<String, Action>> actions = new HashMap<>();
	
	private static final String UTIL_CLASS = "" + 
"class Util {\n" +
"    static Map gs\n" +
"    static call(String method) {\n" +
"        def idx = method.lastIndexOf('.')\n" +
"        call(method.substring(0, idx), method.substring(idx + 1))\n" +
"    }\n" +
"    static call(String method, List args) {\n" +
"        def idx = method.lastIndexOf('.')\n" +
"        call(method.substring(0, idx), method.substring(idx + 1), args)\n" +
"    }\n" +
"    static call(String clazz, String method) {\n" +
"        call(clazz, method, null)" +
"    }\n" +
"    static call(String clazz, String method, List args) {\n" +
"        if (!args)\n" +
"            gs.classes[clazz].\"$method\"()\n" +
"        else\n" +
"            gs.classes[clazz].\"$method\"(*args)\n" +
"    }\n" +
"    static resolveRule(method, player) {\n" +
"        gs.activePlayers = player\n" +
"        gs.choices = call(method)\n" +
"        resolve()\n" + 
"    }\n" +
"    static resolveRule(clazz, method, player) {\n" +
"        gs.activePlayers = player\n" +
"        gs.choices = call(clazz, method)\n" +
"        resolve()\n" +
"    }\n" +
"    static resolve() {\n" +
"        while (!gs.choice)\n" +
"            Thread.sleep(10)\n" +
"        if (gs.choice instanceof String)\n" +
"            call(gs.choice)\n" +
"        else if (gs.choice.args)\n" + 
"            call(gs.choice.action, gs.choice.args)\n" +
"        else\n" +
"            call(gs.choice.action)\n" +
"    }\n" +
"}";

    public ResourceLoader() {

    }

    public Map<String, Map<String, Action>> getActions() {
        return actions;
    }

    public void clearActions() {
        actions = new HashMap<>();
    }

    public void loadActions(String dir) {
        loadActions(new File(dir));
    }

    public void compileResources(String dir) {
        for (String clazz : actions.keySet()) {
            compileResource(dir, clazz, true);
        }
        compileUtilities(dir);
    }

    public void compileUtilities(String dir) {
        exportClass(dir, "Util", UTIL_CLASS);
    }

    public void compileResource(String dir, String clazz, boolean staticCompile) {
        StringBuilder clazzData = new StringBuilder();

        if (clazz.contains(".")) {
            clazzData.append("package ");
            clazzData.append(clazz.substring(0, clazz.lastIndexOf('.')));
            clazzData.append("\n");

            if (false && staticCompile)
                clazzData.append("@groovy.transform.CompileStatic\n");

            clazzData.append("class ");
            clazzData.append(clazz.substring(clazz.lastIndexOf('.') + 1));
            clazzData.append(" {\n");
        }
        else {
            if (false && staticCompile)
                clazzData.append("@groovy.transform.CompileStatic\n");

            clazzData.append("class ");
            clazzData.append(clazz);
            clazzData.append(" {\n");
        }

        clazzData.append("    static Map gs\n");
        clazzData.append("\n");

        for (Action action : actions.get(clazz).values()) {
            String actionData = action.getDefinition();
            actionData = Utils.indent(actionData);
            clazzData.append(actionData);
            clazzData.append("\n");
        }

        clazzData.append("}\n");

        exportClass(dir, clazz, clazzData.toString());
    }

    private void exportClass(String dir, String clazz, String data) {
        System.out.println(data);

        // Make the output folder(s) if it doesn't exist
        String outputDir;
        if (clazz.contains(".")) {
            outputDir = dir + '/' + clazz.substring(0, clazz.lastIndexOf('.')).replace('.', '/');
            clazz = clazz.substring(clazz.lastIndexOf('.') + 1);
        }
        else {
            outputDir = dir;
        }
        new File(outputDir).mkdirs();

        try {
            String fileName = String.format("%s/%s.groovy", outputDir, clazz);
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName), data.length() + 1);
            out.write(data);
            out.close();
        }
        catch (IOException e) {
            System.out.println("Failed to generate class for " + clazz);
            e.printStackTrace();
        }
    }

    private void loadActions(File dir) {
        if (!dir.isDirectory()) {
            System.out.println("No action directory found: " + dir.getPath());
        }

        for (File file : dir.listFiles()) {
            if (!file.isDirectory())
                continue;

            if (file.getName().equalsIgnoreCase("Util")) {
                System.out.println("Util is a reserved name, skipping");
                continue;
            }

            loadActions(file, "");
        }
    }

    private void loadActions(File dir, String pkg) {
        String className = dir.getName();
        className = Character.toUpperCase(className.charAt(0)) + className.substring(1);
        String pkgName = Character.toLowerCase(className.charAt(0)) + className.substring(1);
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                loadActions(file, pkg + pkgName + '.');
                continue;
            }

            if (!file.isFile() || !file.getName().endsWith(".act"))
                continue;

            try {
                ObjectMap data = (ObjectMap)JSON.parse(FileUtils.readFile(file));
                String name = file.getName().substring(0, file.getName().length() - 4);
                addAction(pkg, className, name, data);
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

    private void addAction(String pkg, String clazz, String name, ObjectMap data) {
        String fullClazz = pkg + clazz;
        if (!actions.containsKey(fullClazz))
            actions.put(fullClazz, new HashMap<String, Action>());
        actions.get(fullClazz).put(name, Action.loadAction(data, name));
    }
}
