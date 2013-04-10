package com.github.Jikoo.BookSuite.permissions;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class Permissions {

	boolean enabled = false;
	HashMap<String, PermissionAttachment> attachments = new HashMap<String, PermissionAttachment>();

	public void addDefaultPermissions(Player p) {
		if (!attachments.containsKey(p.getName())) {
			attachments.put(p.getName(), p.addAttachment(Bukkit.getPluginManager().getPlugin("BookSuite")));
		}
		attachments.get(p.getName()).setPermission("booksuite.default", true);
	}

	public void addOpPermissions(Player p) {
		if (!attachments.containsKey(p.getName())) {
			attachments.put(p.getName(), p.addAttachment(Bukkit.getPluginManager().getPlugin("BookSuite")));
		}
		attachments.get(p.getName()).setPermission("booksuite.admin", true);
	}

	public void removePermissions(String pName) {
		try {
			attachments.remove(pName).remove();
		} catch (NullPointerException e) {
			//Player did not have registered permissions and cannot be removed; do nothing.
		}
	}

	public void removeAllPermissions() {
		for (String pName : attachments.keySet()) {
			removePermissions(pName);
		}
	}
}
