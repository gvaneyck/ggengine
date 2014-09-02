package com.gvaneyck.ggengine.gamestate;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.List;
import java.util.Map;

public class GameStateSerializer {
    public String serializeGameState(Map gs, int player) {
        return serialize(gs, player);
    }

    protected String serialize(Object value, int player) {
        if (value instanceof Number) {
            return value.toString();
        }
        else if (value instanceof String) {
            return "\"" + value.toString() + "\"";
        }
        else if (value instanceof Map) {
            return serialize((Map)value, player);
        }
        else if (value instanceof List) {
            return serialize((List)value, player);
        }
        else {
            return serializeObject(value, player);
        }
    }

    protected String serialize(Map m, int player) {
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');

        Object[] entries = m.entrySet().toArray();
        for (int i = 0; i < entries.length; i++) {
            Map.Entry e = (Map.Entry)entries[i];

            buffer.append('"');
            buffer.append(e.getKey().toString());
            buffer.append("\":");

            Object value = e.getValue();
            buffer.append(serialize(value, player));

            if (i < entries.length - 1) {
                buffer.append(',');
            }
        }

        buffer.append('}');

        return buffer.toString();
    }

    protected String serialize(List l, int player) {
        StringBuilder buffer = new StringBuilder();

        buffer.append('[');

        for (int i = 0; i < l.size(); i++) {
            buffer.append(serialize(l.get(i), player));
            if (i < l.size() - 1) {
                buffer.append(',');
            }
        }

        buffer.append(']');

        return buffer.toString();
    }

    protected String serializeObject(Object value, int player) {
        Map properties = DefaultGroovyMethods.getProperties(value);
        properties.remove("class");
        properties.remove("declaringClass");
        properties.remove("metaClass");
        properties.remove("gm");
        properties.remove("gs");
        return serialize(properties, player);
    }
}
