package com.github.Jikoo.BookSuite.io.json;

import java.util.Iterator;
import java.util.LinkedList;

public class JsonValue implements Iterable<JsonValue> {
    
    private static JsonValue parse(char[] Json, I i) {
        JsonValue co = null;
        while(i.i < Json.length) {
            while (Json[i.i] == ' ' || Json[i.i] == ','  || Json[i.i] == ':') {
                i.p();
            }
            
            if (Json[i.i] == '{') {
                co = new JsonObject();
                i.p();
                JsonString key;
                while((key = (JsonString) parse(Json, i)) != null) {
                    JsonValue val = parse(Json, i);
                    ((JsonObject)co).add(key, val);
                }
                return co;
            }
            else if (Json[i.i] == '[') {
                co = new JsonArray();
                i.p();
                JsonValue key;
                while((key = parse(Json, i)) != null) {
                    ((JsonArray)co).add(key);
                }
                return co;
            }
            else if (Json[i.i] == 'n' || Json[i.i] == 'N') {
                co = new JsonNull();
                i.p(4);
                return co;
            }
            else if (Json[i.i] == 't' || Json[i.i] == 'T') {
                co = new JsonBoolean(true);
                i.p(4);
                return co;
            }
            else if (Json[i.i] == 'f' || Json[i.i] == 'F') {
                co = new JsonBoolean(false);
                i.p(5);
                return co;
            }
            else if (Json[i.i] == '"') {
                StringBuilder sb = new StringBuilder();
                i.p();
                while(Json[i.i] != '"') {
                    if (Json[i.i] == '\\') {
                    sb.append(Json[i.i+1]);
                    i.p(2);
                    }
                    else {
                    sb.append(Json[i.i]);
                    i.p();
                    }
                }
                i.p();
                return new JsonString(sb.toString());
            }
            else if (Json[i.i] == ']') {
                i.p();
                return co;
            }
            else if (Json[i.i] == '}') {
                i.p();
                return co;
            }
            else if (isNumber(Json[i.i])) {
                StringBuilder sb = new StringBuilder();
                while(isNumber(Json[i.i])){
                    sb.append(Json[i.i]);
                    i.p();
                }
                return new JsonNumber(sb.toString());
            }
            i.p();
        }
        return new JsonNull();
    }
    
    private static boolean isNumber(char c){
        return c=='1' || c=='2' || c=='3' || c=='4' || c=='5' 
            || c=='6' || c=='7' || c=='8' || c=='9' || c=='0';
    }
    
    protected JsonValue(){}
    
    
    
    
    /**
     * the only approved way to generate a JSON object
     * 
     * @param JSON the input string as JSON
     * @return an objectified version of the JSON string
     */
    public static JsonValue getJsonValue(String JSON) {
        return parse(JSON.toCharArray(), new I());
    }
    
    /**
     * 
     * @param s the name of the field in the JSON object
     * @return the JSON value by that name
     * @throws JsonException if the object is not a JSON object, 
     *     there is no way to get a child by name, and an error is thrown
     */
    public JsonValue get(String s) throws JsonException{
        throw new JsonException("this is not an object");
    }
    
    /**
     * 
     * @param s if you have a JSON string, and you want to use is as a way to get a JSON value from a JSON object, this is how to do it
     * @return the JSON value by that name
     * @throws JsonException if this object is not a JSON object
     */
    public JsonValue get(JsonString s) throws JsonException{
        throw new JsonException("this is not an object");
    }
    
    /**
     * 
     * @param i the index of the JSON array
     * @return the JSON value at the given index of the array
     * @throws JsonException if this object is not a JSON array, 
     *     elements cannot be accessed by index
     */
    public JsonValue get(int i) throws JsonException {
        throw new JsonException("this is not an array");
    }
    
    /**
     * 
     * @return the type of JSON value that this object represents
     */
    public String getType() {
        return "JsonValue"; 
    }

	public Iterator<JsonValue> iterator() {
		return new LinkedList<JsonValue>().iterator();
	}

	public String valueOf() {
		return "";
	}
}

class I {
    int i = 0;
    void p(){i++;}
    void p(int i){this.i+=i;}
}