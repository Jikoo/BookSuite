/**
 * 
 */
package com.github.Jikoo.BookSuite.update;

import java.io.File;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.github.Jikoo.BookSuite.BSLogger;
import com.github.Jikoo.BookSuite.BookSuite;

/**
 * @author Jikoo
 * 
 */
public class UpdateStrings {
	BookSuite plugin;

	public UpdateStrings(BookSuite plugin) {
		this.plugin = plugin;
	}

	public boolean update() {
		YamlConfiguration strings = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "strings.yml"));
		YamlConfiguration defaultStrings = YamlConfiguration.loadConfiguration(plugin.getResource("strings.yml"));
		Set<String> options = defaultStrings.getKeys(false);
		Set<String> current = strings.getKeys(false);
		boolean changed = false;

		for (String s : options) {
			if (!current.contains(s)) {
				strings.set(s, defaultStrings.get(s));
				changed = true;
			}
		}

		for (String s : current) {
			if (!options.contains(s)) {
				strings.set(s, null);
				changed = true;
			}
		}

		strings.options().copyHeader(true);

		if (changed) {
			try {
				strings.save(new File(plugin.getDataFolder(), "strings.yml"));
			} catch (Exception e) {
				BSLogger.err(e);
			}
		}

		return changed;
	}
}
