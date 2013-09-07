/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn - ideas and implementation
 *     Ted Meyer - IO assistance and BML (Book Markup Language)
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
	String dataFolder;

	FileConfiguration aliasYML;

	enum AliasType {
		MULTI, DEFAULT, NONE
	};

	AliasType type = AliasType.DEFAULT;

	private static Alias instance;

	protected static Alias getInstance() {
		if (instance == null)
			instance = new Alias();
		return instance;
	}

	/**
	 * Loads aliases from file if allowed in the config.
	 * 
	 */
	public void enable() {
		dataFolder = BookSuite.getInstance().getDataFolder().getPath();
		try {
			type = AliasType.valueOf(BookSuite.getInstance().getConfig().getString("alias-mode")
					.toUpperCase());
			if (type.equals(AliasType.MULTI)) {
				File aliasFile = new File(dataFolder, "aliases.yml");
				if (aliasFile.exists()) {
					aliasYML = YamlConfiguration.loadConfiguration(aliasFile);
				} else {
					aliasYML = new YamlConfiguration();
				}
			}
		} catch (IllegalArgumentException e) {
			this.setDefault();
		} catch (IllegalStateException e) {
			this.setDefault();
		}
	}

	/**
	 * Helper method for loading aliases because beautiful multi-catches aren't
	 * available 1.6
	 */
	private void setDefault() {
		type = AliasType.DEFAULT;
		BookSuite.getInstance().getConfig().set("alias-mode", "default");
		BookSuite.getInstance().saveConfig();
		BSLogger.warn("Invalid alias type in config, using default setting.");
	}

	/**
	 * Saves aliases to file if in the correct mode. Mode is set in config.yml.
	 */
	public void save() {
		if (type.equals(AliasType.MULTI)) {
			try {
				aliasYML.save(new File(dataFolder, "aliases.yml"));
			} catch (IOException e) {
				BSLogger.warn("Could not save aliases.yml!");
			}
		}
	}

	/**
	 * Adds an alias to a player.
	 * 
	 * @param pName
	 *            the name of the player to whom the alias is being added
	 * @param newAlias
	 *            the alias to add
	 * @return true if the alias was added successfully
	 */
	public boolean addAlias(String pName, String newAlias) {
		if (aliasYML.getStringList(pName).contains(newAlias))
			return false;
		ArrayList<String> aliasList = (ArrayList<String>) aliasYML.getStringList(pName);
		aliasList.add(newAlias);
		aliasYML.set(pName, aliasList);
		save();
		return true;
	}

	/**
	 * Adds an alias to a targeted player. Console-friendly.
	 * 
	 * @param s
	 *            the sender of the command
	 * @param target
	 *            the player to whom the alias is being added
	 * @param newAlias
	 *            the alias to add
	 * @param warn
	 *            true if the player is to be informed of the change
	 */
	public void addAliasToTarget(CommandSender s, Player target, String newAlias, boolean warn) {
		if (!type.equals(AliasType.MULTI)) {
			s.sendMessage(ChatColor.DARK_RED
					+ "Additional aliases are not allowed in the configuration. Please contact your server administrator.");
			return;
		}
		if (!addAlias(target.getName(), newAlias)) {
			s.sendMessage(ChatColor.DARK_RED + target.getName() + " already has the alias "
					+ newAlias + "!");
			return;
		} else {
			s.sendMessage(ChatColor.DARK_GREEN + "Added alias \"" + newAlias + "\" to "
					+ target.getName() + "!");
			if (warn) {
				target.sendMessage(ChatColor.DARK_GREEN + s.getName() + " added " + newAlias
						+ " to your list of aliases!");
			}
		}
	}

	/**
	 * Removes an alias from a player.
	 * 
	 * @param pName
	 *            the name of the player from whom the alias is being removed
	 * @param oldAlias
	 *            the alias to remove
	 * @return true if the alias was removed successfully
	 */
	public boolean removeAlias(String pName, String oldAlias) {
		if (!aliasYML.getStringList(pName).contains(oldAlias)) {
			return false;
		}
		ArrayList<String> aliasList = (ArrayList<String>) aliasYML.getStringList(pName);
		aliasList.remove(oldAlias);
		aliasYML.set(pName, aliasList);
		save();
		return true;
	}

	/**
	 * Remove an alias from a player. Console-friendly.
	 * 
	 * @param s
	 *            the sender of the command
	 * @param target
	 *            the player from whom the alias is being removed
	 * @param oldAlias
	 *            the alias to remove
	 * @param warn
	 *            true if the player is to be informed of the change
	 */
	public void removeAliasFromTarget(CommandSender s, Player target, String oldAlias, boolean warn) {
		if (!type.equals(AliasType.MULTI)) {
			s.sendMessage(ChatColor.DARK_RED
					+ "Additional aliases are not allowed in the configuration. Please contact your server administrator.");
			return;
		}
		if (!removeAlias(target.getName(), oldAlias)) {
			s.sendMessage(ChatColor.DARK_RED + target.getName() + " doesn't have the alias "
					+ oldAlias + "!");
			return;
		} else {
			s.sendMessage(ChatColor.DARK_GREEN + "Added alias \"" + oldAlias + "\" to "
					+ target.getName() + "!");
			if (warn) {
				target.sendMessage(ChatColor.DARK_GREEN + s.getName() + " added " + oldAlias
						+ " to your list of aliases!");
			}
		}
	}

	/**
	 * Sets the current alias of the player, provided that the player has the
	 * alias.
	 * 
	 * @param p
	 *            the player involved
	 * @param active
	 *            the alias to set active
	 * @return true if the alias was set
	 */
	public boolean setActiveAlias(Player p, String active) {
		if (getAliases(p).contains(active)) {
			aliasYML.set("current." + p.getName(), active);
			save();
			return true;
		}
		return false;
	}

	/**
	 * Sets the active alias of another player. Console-friendly.
	 * 
	 * @param s
	 *            the sender of the command
	 * @param target
	 *            the player whose alias is being set
	 * @param active
	 *            the alias to set active
	 * @param warn
	 *            true if the player is to be informed of the change
	 */
	public void setTargetActiveAlias(CommandSender s, Player target, String active, boolean warn) {
		if (setActiveAlias(target, active)) {
			s.sendMessage(ChatColor.DARK_GREEN + target.getName() + "'s active alias set to "
					+ active + "!");
			if (warn) {
				target.sendMessage(ChatColor.DARK_GREEN + s.getName()
						+ " set your active alias to " + active + "!");
			}
		} else {
			s.sendMessage(ChatColor.DARK_RED + target.getName() + "does not have the alias "
					+ active + "!");
		}
	}

	/**
	 * Returns the active alias of the given player.
	 * 
	 * @param p
	 *            the player
	 * @return
	 */
	public String getActiveAlias(Player p) {
		switch (type) {
		case NONE:
			return p.getName();
		case MULTI:
			String current = aliasYML.getString("current." + p.getName());
			if (getAliases(p).contains(current)) {
				return current;
			}
			aliasYML.set("current." + p.getName(), p.getDisplayName());
			save();
		case DEFAULT:
		default:
			return p.getDisplayName();
		}
	}

	/**
	 * Returns a list of all known aliases of the given player.
	 * 
	 * @param p
	 *            the player
	 * @return all aliases of the player p
	 */
	public ArrayList<String> getAliases(Player p) {
		ArrayList<String> aliases = new ArrayList<String>();
		aliases.add(p.getName());
		switch (type) {
		case MULTI:
			aliases.add(p.getDisplayName());
			for (String s : aliasYML.getStringList(p.getName())) {
				aliases.add(s);
			}
			break;
		case DEFAULT:
			aliases.add(p.getDisplayName());
			break;
		default:
			break;
		}
		return aliases;
	}
}
