package com.github.Jikoo.BookSuite.rules;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import com.github.Jikoo.BookSuite.BookSuite;

public class Rules implements CommandExecutor {

	BookSuite plugin;

	File ruleFile;
	FileConfiguration ruleYML;

	private static Rules instance;
	private Rules(BookSuite bs){this.plugin = bs;}
	public static Rules getInstance(BookSuite bs){
		if (instance == null) instance = new Rules(bs);
		return instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args) {
		if (args.length == 0) {
			
			//do rulebook getting
		}
		
		if (args.length >= 1) {
			if (args[0].equals("add") || args[0].equals("a")) {
				Set<String> ruleEntries = ruleYML.getConfigurationSection("books").getKeys(false);
				HashMap<Integer, String> newRules = new HashMap<Integer, String>();
				for (String entry : ruleEntries) {
					try {
						Integer entryNumber = Integer.parseInt(entry);
						
						
						
						
						newRules.put(entryNumber, ruleYML.getString("books." + entry));
					} catch(NumberFormatException e) {}//if it isn't an int, it doesn't belong anyway. Delete entry.
				}
				//make rules.yml entry
				//make file
				
			} else if (args[0].equals("del") || args[0].equals("delete") || args[0].equals("d")) {
				//remove entry
				//remove file
				
			} else if (args[0].equals("list")) {
				//list entries in rules.yml in order
				
				
			} else if (args[0].equals("set")) {
				//set entry specified to 
				
				
			} else if (args[0].equals("adddefault")) {
				//add book# to default /rules give list
				
				
			} else if (args[0].equals("deldefault")) {
				//remove from give list
				
				
			} else if (args[0].equals("listdefault")) {
				//list current give list
				
				
			} else if (args[0].equals("swap")) {
				//list entries in rules.yml in order
				
				
			} else {
				
				
				//do rulebook getting
			}
		}
		
		
		
		return false;
	}
	
	
	
	public void load() {
		ruleFile = new File(plugin.getDataFolder() + "/Rules/", "rules.yml");
		if (ruleFile.exists()) {
			ruleYML = YamlConfiguration.loadConfiguration(ruleFile);
		} else {
			ruleYML = new YamlConfiguration();
		}
	}
	
	
	
	public void save() {
		ruleFile = new File(plugin.getDataFolder() + "/Rules/", "rules.yml");
		try {
			if (!ruleFile.exists()) {
				ruleFile.createNewFile();
			}
			ruleYML.save(ruleFile);
		} catch (IOException e) {
			plugin.getLogger().warning(ChatColor.DARK_RED + "Could not save rules.yml!");
		}
	}
	
	
	
	
}
