/*******************************************************************************
 * Copyright (c) 2013 Ted Meyer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.github.Jikoo.BookSuite.struct.json;

public class JsonNull extends JsonValue{
    /**
     *  The empty constructor for the JSON Null wrapper class.
     */
    public String toString(){
        return "NULL";
    }
    
    @Override
    public String getType() {
        return "JsonNull"; 
    }
    
    @Override
    public boolean equals(Object o) {
        return o instanceof JsonNull;
    }
    
    @Override
    public int hashCode() {
        return 0;
    }
}
