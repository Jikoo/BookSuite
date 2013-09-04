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
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.Jikoo.BookSuite.BookSuite;

public class Rules implements CommandExecutor, Listener {

	BookSuite plugin = BookSuite.getInstance();

	File ruleContainer = new File(plugin.getDataFolder() + "/Rules/Books/");;
	File ruleFile = new File(plugin.getDataFolder() + "/Rules/", "rules.yml");;
	FileConfiguration ruleYML;

	CommandExecutor originalRules;
	CommandExecutor originalQuestion;

	public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args) {
		if (args.length == 0) {
			
			//do rulebook getting
			// things of note:
			// - villagers
			// - rule updates
			
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
				//if in default, remove
				
			} else if (args[0].equals("list")) {
				//list entries in rules.yml in order
				
				
			} else if (args[0].equals("set") || args[0].equals("update")) {
				//set entry specified to 
				
				
			} else if (args[0].equals("adddefault")) {
				//add book# to default /rules give list
				
				
			} else if (args[0].equals("deldefault")) {
				//remove from give list
				
				
			} else if (args[0].equals("listdefault")) {
				//list current give list
				
				
			} else if (args[0].equals("swap")) {
				//swap
				
				
			} else if (args[0].equals("insert")) {
				//... these are relatively self-explanatory, really.
				
				
			} else {
				
				
				//do rulebook getting
			}
		}
		
		
		
		return false;
	}
	
	
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		//TODO
		//check if user has received up-to-date rules
		//if no, give book synchronously
		//TODO rewrite importing ;-;
	}
	
	
	
	public void renameBookFile(String currentName, String newName) {
		if (ruleContainer.exists()) {
			File f = new File(ruleContainer, currentName);
			if (f.exists()) {
				File target = new File(ruleContainer, newName);
				if (target.exists())//TODO refine for swaps - temp files?
					target.delete();
				f.renameTo(target);
			}
		}
	}
	
	
	
	
	
	public void load() {
		if (ruleFile.exists()) {
			ruleYML = YamlConfiguration.loadConfiguration(ruleFile);
		} else {
			ruleYML = new YamlConfiguration();
		}
		
	}



	public void save() {
		try {
			if (!ruleFile.exists()) {
				ruleFile.createNewFile();
			}
			ruleYML.save(ruleFile);
		} catch (IOException e) {
			plugin.getLogger().warning(ChatColor.DARK_RED + "Could not save rules.yml!");
		}
	}



	public void disable() {
		HandlerList.unregisterAll(this);
		plugin.getServer().getPluginCommand("rules").setExecutor(originalRules);
		plugin.getServer().getPluginCommand("?").setExecutor(originalQuestion);
	}



	public void enable() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		originalRules = plugin.getServer().getPluginCommand("rules").getExecutor();
		originalQuestion = plugin.getServer().getPluginCommand("?").getExecutor();
		plugin.getServer().getPluginCommand("rules").setExecutor(this);
		plugin.getServer().getPluginCommand("?").setExecutor(this);
		load();
	}
}
