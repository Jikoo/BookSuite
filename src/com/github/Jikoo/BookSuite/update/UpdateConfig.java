package com.github.Jikoo.BookSuite.update;

import java.util.Set;

import com.github.Jikoo.BookSuite.BookSuite;

public class UpdateConfig {

	BookSuite plugin;

	public UpdateConfig(BookSuite plugin) {
		this.plugin = plugin;
	}
	
	public void update() {
		Set<String> options = plugin.getConfig().getKeys(false);
		boolean changed = false;
		
		for (String s : options) {
			if (plugin.getConfig().get(s) == null) {
				plugin.getConfig().set(s, plugin.getConfig().getDefaults().get(s));
				changed = true;
			}
		}
		
		if (changed)
			plugin.saveConfig();
	}
}
