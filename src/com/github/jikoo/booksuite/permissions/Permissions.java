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
package com.github.jikoo.booksuite.permissions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class Permissions {

	Map<UUID, PermissionAttachment> attachments = new HashMap<UUID, PermissionAttachment>();

	public void addDefaultPermissions(Player p) {
		if (!attachments.containsKey(p.getUniqueId())) {
			attachments.put(p.getUniqueId(),
					p.addAttachment(Bukkit.getPluginManager().getPlugin("BookSuite")));
		}
		attachments.get(p.getUniqueId()).setPermission("booksuite.default", true);
	}

	public void addOpPermissions(Player p) {
		if (!attachments.containsKey(p.getUniqueId())) {
			attachments.put(p.getUniqueId(),
					p.addAttachment(Bukkit.getPluginManager().getPlugin("BookSuite")));
		}
		attachments.get(p.getUniqueId()).setPermission("booksuite.admin", true);
	}

	public void removePermissions(UUID pUUID) {
		if (attachments.containsKey(pUUID)) {
			attachments.remove(pUUID).remove();
		}
	}

	public void removeAllPermissions() {
		UUID[] uuids = attachments.keySet().toArray(new UUID[0]);
		for (int i = 0; i < uuids.length; i++) {
			removePermissions(uuids[i]);
		}
	}
}
