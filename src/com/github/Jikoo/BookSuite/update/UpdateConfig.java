/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn- initial API and implementation
 *     Ted Meyer - some help
 ******************************************************************************/
package com.github.Jikoo.BookSuite.update;

import java.util.Set;

import com.github.Jikoo.BookSuite.BookSuite;

public class UpdateConfig {

	BookSuite plugin;

	public UpdateConfig(BookSuite plugin) {
		this.plugin = plugin;
	}
	
	public boolean update() {
		Set<String> options = plugin.getConfig().getDefaults().getKeys(false);
		Set<String> current = plugin.getConfig().getKeys(false);
		boolean changed = false;
		
		if (plugin.getConfig().contains("use-external-permissions")) {
			plugin.getConfig().set("use-internal-permissions", !plugin.getConfig().getBoolean("use-external-permissions"));
			changed = true;
		}
		
		for (String s : options) {
			if (!current.contains(s)) {
				plugin.getConfig().set(s, plugin.getConfig().getDefaults().get(s));
				changed = true;
			}
		}
		
		
		for (String s : current) {
			if (!options.contains(s)) {
				plugin.getConfig().set(s, null);
				changed = true;
			}
		}
		
		
		plugin.getConfig().options().copyHeader(true);
		
		
		if (changed) {
			plugin.saveConfig();
		}
		
		return changed;
	}
}
