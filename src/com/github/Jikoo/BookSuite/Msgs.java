/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted Meyer - switched whole system from yaml to json
 ******************************************************************************/
package com.github.Jikoo.BookSuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.github.Jikoo.BookSuite.io.json.JsonException;
import com.github.Jikoo.BookSuite.io.json.JsonString;
import com.github.Jikoo.BookSuite.io.json.JsonValue;

public class Msgs {
	private static JsonValue messages;

	private static void parse() throws FileNotFoundException {
		String path = BookSuite.getInstance().getDataFolder() + "/strings.json";
		Scanner reader = new Scanner(new File(path));
		StringBuilder sb = new StringBuilder();
		while (reader.hasNext()) {
			sb.append(reader.next());
		}
		reader.close();
		String s = sb.toString();
		messages = JsonValue.getJsonValue(s);
	}

	public static String getMessage(String s) {
		if (messages == null) {
			try {
				parse();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		try {
			return messages.get(new JsonString(s)).valueOf();
		} catch (JsonException e) {
			e.printStackTrace();
			return "";
		}
	}
}
