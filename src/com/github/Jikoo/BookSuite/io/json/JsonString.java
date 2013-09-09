package com.github.Jikoo.BookSuite.io.json;

public class JsonString extends JsonValue {

	private String s;

	/**
	 * 
	 * @param s
	 *            the string for this JSON String to represent.
	 */
	public JsonString(String s) {
		this.s = s;
	}

	/**
	 * 
	 * @return the actual value
	 */
	public String valueOf() {
		return s;
	}

	@Override
	public String toString() {
		return "\"" + this.s + "\"";
	}

	@Override
	public String getType() {
		return "JsonString";
	}

	@Override
	public int hashCode() {
		return s.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof JsonString) {
			return ((JsonString) o).s.equals(s);
		}
		return false;
	}
}
