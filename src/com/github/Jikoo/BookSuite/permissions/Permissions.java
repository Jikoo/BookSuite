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
package com.github.Jikoo.BookSuite.permissions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class Permissions {

	boolean enabled = false;
	Map<Object, PermissionAttachment> attachments =
			new HashMap<Object, PermissionAttachment>();

	public void addDefaultPermissions(Player p) {
		if (!attachments.containsKey(p.getName())) {
			attachments.put(p.getName(),
					p.addAttachment(Bukkit.getPluginManager().getPlugin("BookSuite")));
		}
		attachments.get(p.getName()).setPermission("booksuite.default", true);
	}

	public void addOpPermissions(Player p) {
		if (!attachments.containsKey(p.getName())) {
			attachments.put(p.getName(),
					p.addAttachment(Bukkit.getPluginManager().getPlugin("BookSuite")));
		}
		attachments.get(p.getName()).setPermission("booksuite.admin", true);
	}

	public void removePermissions(Object pName) {
		try {
			attachments.remove(pName).remove();
		} catch (NullPointerException e) {
			// Player did not have registered permissions and cannot be removed;
			// do nothing.
		}
	}

	public void removeAllPermissions() {
		Object[] names = this.attachments.keySet().toArray();
		for (int i = 0; i < names.length; i++) {
			this.removePermissions(names[i]);
		}
	}
}
