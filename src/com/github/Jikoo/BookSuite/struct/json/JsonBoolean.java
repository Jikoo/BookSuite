package com.github.Jikoo.BookSuite.struct.json;

public class JsonBoolean extends JsonValue{
    private boolean b;
    
    public boolean value() {
        return b;
    }
    
    public JsonBoolean(boolean b) {
        this.b = b;
    }
    
    @Override
    public String toString() {
        return b ? "TRUE" : "FALSE";
    }
    
    @Override
    public String getType() {
        return "JsonBoolean"; 
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof JsonBoolean) {
            return b == ((JsonBoolean)o).b;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return b?1:0;
    }
}
