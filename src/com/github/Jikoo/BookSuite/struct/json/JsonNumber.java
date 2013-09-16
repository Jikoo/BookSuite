/*******************************************************************************
 * Copyright (c) 2013 Ted Meyer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.github.Jikoo.BookSuite.struct.json;

public class JsonNumber extends JsonValue{
    
    private String v;
    
    public JsonNumber(String v) {
        this.v = v;
    }
    
    @Override
    public String toString() {
        return v;
    }
    
    @Override
    public String getType() {
        return "JsonNumber"; 
    }
    
    @Override
    public int hashCode() {
        return v==null?0:v.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof JsonNumber) {
            return this.v.equals(((JsonNumber)o).v);
        }
        return false;
    }
}
