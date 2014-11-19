/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn
 ******************************************************************************/
package com.github.Jikoo.BookSuite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bukkit.configuration.file.YamlConfiguration;

public class Msgs {
	private static YamlConfiguration strings;
	public Msgs() {
		File f = new File(BookSuite.getInstance().getDataFolder(), "strings.yml");
		if (!f.exists()) {
			InputStream stream = BookSuite.getInstance().getResource("strings.yml");
			InputStreamReader reader = new InputStreamReader(stream);
			strings = YamlConfiguration.loadConfiguration(reader);
			try {
				stream.close();
				reader.close();
			} catch (IOException e) {
				BSLogger.debugWarn("Unable to close streams while loading strings.yml!");
			}
		} else {
			strings = YamlConfiguration.loadConfiguration(f);
		}
	}

	public String get(String s) {
		String msg = BookSuite.getInstance().functions.parseBML(strings.getString(s));
		return msg == null ? null : msg.equals("null") ? null : msg;
	}
}
