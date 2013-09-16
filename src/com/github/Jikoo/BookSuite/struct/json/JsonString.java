/*******************************************************************************
 * Copyright (c) 2013 Ted Meyer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.github.Jikoo.BookSuite.struct.json;

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
