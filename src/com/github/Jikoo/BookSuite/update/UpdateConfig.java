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
		
		if(plugin.getConfig().contains("use-external-permissions")) {
			plugin.getConfig().set("use-internal-permissions", !plugin.getConfig().getBoolean("use-external-permissions"));
			changed = true;
		}
		
		for (String s : options) {
			if (!current.contains(s)) {
				plugin.getConfig().set(s, plugin.getConfig().getDefaults().get(s));
				changed = true;
			}
		}
		
		if (changed) {
			plugin.saveConfig();
		}
		
		return changed;
	}
}
