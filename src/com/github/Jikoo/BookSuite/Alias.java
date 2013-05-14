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
package com.github.Jikoo.BookSuite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Alias {
	
	BookSuite plugin = BookSuite.getInstance();
	
	File aliasFile;
	FileConfiguration aliasYML;
	
	enum aliasType {MULTI, DEFAULT};
	aliasType type;
	
	private static Alias instance;
	public static Alias getInstance() {
		if (instance == null) instance = new Alias();
		return instance;
	}
	
	
	public void load() {
		type = aliasType.valueOf(plugin.getConfig().getString("alias-mode").toUpperCase());
		if (type.equals(aliasType.MULTI)) {
			aliasFile = new File(plugin.getDataFolder(), "aliases.yml");
			if (aliasFile.exists()) {
				aliasYML = YamlConfiguration.loadConfiguration(aliasFile);
			} else {
				aliasYML = new YamlConfiguration();
			}
		}
	}
	
	
	public void save() {
		aliasFile = new File(plugin.getDataFolder(), "aliases.yml");
		try {
			if (!aliasFile.exists()) {
				aliasFile.createNewFile();
			}
			aliasYML.save(aliasFile);
		} catch (IOException e) {
			plugin.getLogger().warning(ChatColor.DARK_RED + "Could not save aliases.yml!");
		}
	}
	
	
	public boolean addAlias(String pName, String newAlias) {
		if (aliasYML.getStringList(pName).contains(newAlias))
			return false;
		ArrayList<String> aliasList = (ArrayList<String>) aliasYML.getStringList(pName);
		aliasList.add(newAlias);
		aliasYML.set(pName, aliasList);
		save();
		return true;
	}
	
	
	public void addAliasToTarget(CommandSender s, Player target, String newAlias, boolean warn) {
		if (!type.equals(aliasType.MULTI)) {
			s.sendMessage(ChatColor.DARK_RED + "Additional aliases are not allowed in the configuration. Please contact your server administrator.");
			return;
		}
		if (!addAlias(target.getName(), newAlias)) {
			s.sendMessage(ChatColor.DARK_RED+target.getName() + " already has the alias " + newAlias + "!");
			return;
		} else {
			s.sendMessage(ChatColor.DARK_GREEN + "Added alias \"" + newAlias + "\" to " + target.getName() + "!");
			if (warn)
				target.sendMessage(ChatColor.DARK_GREEN + s.getName() + " added " + newAlias + " to your list of aliases!");
		}
	}
	
	
	public boolean removeAlias(String pName, String oldAlias) {
		if (!aliasYML.getStringList(pName).contains(oldAlias))
			return false;
		ArrayList<String> aliasList = (ArrayList<String>) aliasYML.getStringList(pName);
		aliasList.remove(oldAlias);
		aliasYML.set(pName, aliasList);
		save();
		return true;
	}
	
	
	public void removeAliasFromTarget(CommandSender s, Player target, String oldAlias, boolean warn) {
		if (!type.equals(aliasType.MULTI)) {
			s.sendMessage(ChatColor.DARK_RED + "Additional aliases are not allowed in the configuration. Please contact your server administrator.");
			return;
		}
		if (!removeAlias(target.getName(), oldAlias)) {
			s.sendMessage(ChatColor.DARK_RED+target.getName() + " doesn't have the alias " + oldAlias + "!");
			return;
		} else {
			s.sendMessage(ChatColor.DARK_GREEN + "Added alias \"" + oldAlias + "\" to " + target.getName() + "!");
			if (warn)
				target.sendMessage(ChatColor.DARK_GREEN + s.getName() + " added " + oldAlias + " to your list of aliases!");
		}
	}
	
	
	public boolean setActiveAlias(Player p, String active) {
		if (getAliases(p).contains(active)) {
			aliasYML.set("current."+p.getName(), active);
			save();
			return true;
		}
		return false;
	}
	
	
	public void setTargetActiveAlias(CommandSender s, Player target, String active, boolean warn) {
		if (setActiveAlias(target, active)) {
			s.sendMessage(ChatColor.DARK_GREEN + target.getName() + "'s active alias set to " + active + "!");
			if (warn)
				target.sendMessage(ChatColor.DARK_GREEN + s.getName() + " set your active alias to " + active + "!");
		} else s.sendMessage(ChatColor.DARK_RED + target.getName() + "does not have the alias " + active + "!");
	}
	
	
	public String getActiveAlias(Player p) {
		String current = aliasYML.getString("current." + p.getName());
		if (getAliases(p).contains(current))
			return current;
		aliasYML.set("current." + p.getName(), p.getDisplayName());
		save();
		return p.getDisplayName();
	}
	
	
	public ArrayList<String> getAliases(Player p) {
		ArrayList<String> aliases = new ArrayList<String>();
		aliases.add(p.getName());
		switch (aliasType.valueOf(plugin.getConfig().getString("alias-mode"))) {//TODO 1.6 compatability - ENUM
			case MULTI:
				aliases.add(p.getDisplayName());
				for (String s : aliasYML.getStringList(p.getName())) {
					aliases.add(s);
				}
				break;
			case DEFAULT:
				aliases.add(p.getDisplayName());
				break;
			default: break;
		}
		return aliases;
	}
}
