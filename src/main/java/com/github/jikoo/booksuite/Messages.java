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
package com.github.jikoo.booksuite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.bukkit.configuration.file.YamlConfiguration;

class Messages {

	private final YamlConfiguration strings;
	private final BookSuite plugin;

	Messages(BookSuite plugin) {
		this.plugin = plugin;
		File f = new File(plugin.getDataFolder(), "strings.yml");
		if (f.exists()) {
			strings = YamlConfiguration.loadConfiguration(f);
		} else {
			strings = new YamlConfiguration();
		}
		InputStream stream = plugin.getResource("strings.yml");
		InputStreamReader reader = new InputStreamReader(stream);
		strings.setDefaults(YamlConfiguration.loadConfiguration(reader));
		try {
			stream.close();
			reader.close();
		} catch (IOException e) {
			System.err.println("Unable to close streams while loading strings.yml!");
		}
	}

	String get(String s) {
		String msg = plugin.getFunctions().parseBML(strings.getString(s));
		return msg == null ? null : msg.equals("null") ? null : msg;
	}
}
