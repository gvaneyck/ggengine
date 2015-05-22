package com.gvaneyck.ggengine.gamestate;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.List;
import java.util.Map;

public class GameStateSerializer {
    public String serialize(Object value) {
        if (value == null) {
            return "null";
        }
        else if (value instanceof Number) {
            return value.toString();
        }
        else if (value instanceof String) {
            return "\"" + value.toString() + "\"";
        }
        else if (value instanceof Map) {
            return serialize((Map)value);
        }
        else if (value instanceof List) {
            return serialize((List)value);
        }
        else {
            return serializeObject(value);
        }
    }

    protected String serialize(Map m) {
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');

        Object[] entries = m.entrySet().toArray();
        for (int i = 0; i < entries.length; i++) {
            Map.Entry e = (Map.Entry)entries[i];
            Object value = e.getValue();

            if (value != null) {
                buffer.append('"');
                buffer.append(e.getKey().toString());
                buffer.append("\":");

                buffer.append(serialize(value));

                if (i < entries.length - 1) {
                    buffer.append(',');
                }
            }
        }

        buffer.append('}');

        return buffer.toString();
    }

    protected String serialize(List l) {
        StringBuilder buffer = new StringBuilder();

        buffer.append('[');

        for (int i = 0; i < l.size(); i++) {
            buffer.append(serialize(l.get(i)));
            if (i < l.size() - 1) {
                buffer.append(',');
            }
        }

        buffer.append(']');

        return buffer.toString();
    }

    protected String serializeObject(Object value) {
        Map properties = DefaultGroovyMethods.getProperties(value);
        properties.remove("class");
        properties.remove("declaringClass");
        properties.remove("metaClass");
        properties.remove("gm");
        properties.remove("gs");
        return serialize(properties);
    }
}
