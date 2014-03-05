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

import java.util.ArrayList;

import org.bukkit.entity.Player;

public class Alias {

	private boolean enabled;

	private static Alias instance;

	protected static Alias getInstance() {
		if (instance == null)
			instance = new Alias();
		return instance;
	}

	/**
	 * Sets aliases enabled.
	 */
	public void enable() {
		enabled = true;
	}

	/**
	 * Sets aliases disabled.
	 */
	public void disable() {
		enabled = false;
	}

	/**
	 * Gets the name used to sign books for a player.
	 * 
	 * @param p the player to get active alias of
	 * 
	 * @return the player's active alias
	 */
	public String getActiveAlias(Player p) {
		if (enabled && p.getDisplayName() != null) {
			return p.getDisplayName();
		}
		return p.getName();
	}

	/**
	 * Returns a list of all known aliases of the given player.
	 * 
	 * @param p the player
	 * 
	 * @return all aliases of the player p
	 */
	public ArrayList<String> getAliases(Player p) {
		ArrayList<String> aliases = new ArrayList<String>();
		aliases.add(p.getName());
		if (enabled && p.getDisplayName() != null) {
			aliases.add(p.getDisplayName());
		}
		return aliases;
	}
}
