package com.github.Jikoo.BookSuite.io.json;

import java.util.ArrayList;
import java.util.Iterator;

public class JsonArray extends JsonValue implements Iterable<JsonValue> {
    private ArrayList<JsonValue> values = new ArrayList<JsonValue>();
    
    @Override
    public JsonValue get(int i) {
        return values.get(i);
    }
    
    /**
     * 
     * @param jv the JsonValue to be added to this array
     */
    public void add(JsonValue jv) {
        this.values.add(jv);
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("[");
        for(int i = 0; i < values.size() ; i++) {
            JsonValue jv = values.get(i);
            if (i > 0) result.append(",");
            result.append(jv.toString());
        }
        result.append("]");
        return result.toString();
    }
    
    @Override
    public String getType() {
        return "JsonArray"; 
    }
    
    @Override
    public int hashCode() {
        return values.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof JsonArray) {
            return values.equals(((JsonArray)o).values);
        }
        return false;
    }

	@Override
	public Iterator<JsonValue> iterator() {
		return this.values.iterator();
	}
}
