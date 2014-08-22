package com.gvaneyck.ggengine;

import java.util.List;
import java.util.Map;

public class UndoStep {
    private UndoType type;
    private Object parent;
    private String key;
    private Object value;
    private int idx;
    
    private UndoStep() { }
    
    private UndoStep(UndoType type, Object parent, String key, Object value, int idx) {
        this.type = type;
        this.parent = parent;
        this.key = key;
        this.value = value;
        this.idx = idx;
    }
    
    public static UndoStep makeAdd(Object parent, String key, int val) {
        return new UndoStep(UndoType.ADD, parent, key, val, 0);
    }
    
    public static UndoStep makeInsert(Object parent, Object val, int idx) {
        return new UndoStep(UndoType.INSERT, parent, null, val, idx);
    }
    
    public static UndoStep makeRemove(Object parent, int idx) {
        return new UndoStep(UndoType.REMOVE, parent, null, null, idx);
    }
    
    public void apply() {
        switch (type) {
            case ADD:
                Map<String, Object> map = (Map<String, Object>)parent;
                double newVal = ((Number)map.get(key)).doubleValue() + ((Number)value).doubleValue();
                map.put(key, newVal);
                break;

            case INSERT:
                List list1 = (List)parent;
                list1.add(idx, value);
                break;
            
            case REMOVE:
                List list2 = (List)parent;
                list2.remove(idx);
                break;
        }
    }
}
