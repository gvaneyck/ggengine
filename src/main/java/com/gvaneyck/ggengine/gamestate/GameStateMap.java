package com.gvaneyck.ggengine.gamestate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameStateMap implements Map {
    private List<UndoStep> undos;

    private Map map;

    public GameStateMap(Map map) {
        this.map = map;
    }

    public void mark() {
        if (undos == null) {
            undos = new ArrayList<UndoStep>();
        }
        else {
            undos.get(undos.size() - 1).mark = true;
        }
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Object get(Object key) {
        return map.get(key);
    }

    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    public Object remove(Object key) {
        return map.remove(key);
    }

    public void putAll(Map m) {
        map.putAll(m);
    }

    public void clear() {
        map.clear();
    }

    public Set keySet() {
        return map.keySet();
    }

    public Collection values() {
        return map.values();
    }

    public Set<Entry> entrySet() {
        return map.entrySet();
    }
}
