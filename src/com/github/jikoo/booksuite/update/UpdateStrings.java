/**
 * 
 */
package com.github.jikoo.booksuite.update;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.github.jikoo.booksuite.BSLogger;
import com.github.jikoo.booksuite.BookSuite;

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
		InputStream stream = plugin.getResource("strings.yml");
		InputStreamReader reader = new InputStreamReader(stream);
		YamlConfiguration defaultStrings = YamlConfiguration.loadConfiguration(reader);
		try {
			stream.close();
			reader.close();
		} catch (IOException e) {
			BSLogger.warn("Unable to close streams while reading default config!");
		}
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
