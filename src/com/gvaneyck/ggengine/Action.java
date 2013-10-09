package com.gvaneyck.ggengine;

import org.codehaus.groovy.control.CompilationFailedException;

import com.gvaneyck.util.Utils;
import com.gvaneyck.util.encoding.ObjectMap;

/**
 * Represents an action a player can take
 * 
 * @author Gabriel Van Eyck
 */
public class Action {
    private String name;
    private String code;
    private String[] args;

    private String definition;

    public static Action loadAction(ObjectMap action, String name) {
        String code;
        if (action.containsKey("code")) {
            code = action.getString("code");
            code = Utils.indent(code);

            try {
                Utils.shell.parse(code);
            }
            catch (CompilationFailedException cfe) {
                System.out.println("Failed to parse action '" + name + "'");
                System.out.println(cfe.getMessage());
            }
        }
        else {
            code = "";
            System.out.println("Action '" + name + "' contained no code");
        }

        String[] args;
        if (action.containsKey("args")) {
            args = action.getTypedArray("args", String[].class);
        }
        else {
            args = new String[0];
        }

        return new Action(name, code, args);
    }

    private Action(String name, String code, String[] args) {
        this.name = name;
        this.code = code;
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String[] getArgs() {
        return args;
    }

    public String getDefinition() {
        if (definition == null) {
            StringBuilder buffer = new StringBuilder();

            buffer.append("static ");
            buffer.append(name);
            buffer.append("(");
            for (int i = 0; i < args.length; i++) {
                buffer.append(args[i]);
                if (i < args.length - 1)
                    buffer.append(", ");
            }
            buffer.append(") {\n");

            buffer.append(code);
            buffer.append("\n");

            buffer.append("}\n");

            definition = buffer.toString();
        }

        return definition;
    }

    public String toString() {
        return name + ":\n" + code;
    }
}
