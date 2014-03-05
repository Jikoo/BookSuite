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

import org.bukkit.configuration.file.YamlConfiguration;

public class Msgs {
	private static YamlConfiguration strings;
	public Msgs() {
		String defaultLocation = "plugins" + File.pathSeparatorChar + "BookSuite"
				+ File.pathSeparatorChar;
		File f = new File(defaultLocation, "strings.yml");
		if (f.exists()) {
			strings = YamlConfiguration.loadConfiguration(f);
		} else {
			strings = YamlConfiguration.loadConfiguration(BookSuite.getInstance().getResource(
					"strings.yml"));
		}
	}

	public String get(String s) {
		String msg = BookSuite.getInstance().functions.parseBML(strings.getString(s));
		return msg.equals("null") ? null : msg;
	}
}
