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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;

import com.github.Jikoo.BookSuite.BookSuite;

public class PermissionsListener implements Listener {
	Permissions permissions;
	boolean enabled = false;
	BookSuite plugin;
	Map<UUID, Integer> tasks;

	public PermissionsListener(BookSuite plugin) {
		this.plugin = plugin;
		tasks = new HashMap<UUID, Integer>();
	}

	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
		int taskID = syncImplementPermissions(event.getPlayer());
		if (taskID != -1) {
			tasks.put(event.getPlayer().getUniqueId(), taskID);
		}
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled()) {
			return;
		}
		if (event.getMessage().toLowerCase().contains("op")) {
			String[] command = event.getMessage().toLowerCase()
					.replaceAll("/", "").split(" ");
			if (command[0].equals("op") || command[0].equals("deop")) {
				// While vanilla would use getPlayerExact, we need to
				// compensate for autofill from plugins
				if (command.length == 1) {
					int taskID = syncOpPermissionsCheck(event.getPlayer(), event.getPlayer().isOp());
					if (taskID != -1) {
						tasks.put(event.getPlayer().getUniqueId(), taskID);
					}
					return;
				}
				List<Player> pList = Bukkit.matchPlayer(command[1]);
				for (Player p : pList) {
					if (p != null) {
						int taskID = syncOpPermissionsCheck(p, p.isOp());
						if (taskID != -1) {
							tasks.put(p.getUniqueId(), taskID);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onConsoleCommand(ServerCommandEvent event) {
		if (event.getCommand().toLowerCase().contains("op")) {
			String[] command = event.getCommand().toLowerCase().split(" ");
			if (command.length == 2
					&& (command[0].equals("op") || command[0].equals("deop"))) {
				List<Player> pList = Bukkit.matchPlayer(command[1]);
				for (Player p : pList) {
					if (p != null) {
						int taskID = syncOpPermissionsCheck(p, p.isOp());
						if (taskID != -1) {
							tasks.put(p.getUniqueId(), taskID);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		permissions.removePermissions(event.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onKick(PlayerKickEvent event) {
		permissions.removePermissions(event.getPlayer().getUniqueId());
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void enable() {
		if (!enabled) {
			enabled = true;
			permissions = new Permissions();
			registerListeners();
			registerOnlinePlayers();
		}
	}

	public void disable() {
		if (enabled) {
			enabled = false;
			this.stopAllPendingTasks();
			permissions.removeAllPermissions();
			HandlerList.unregisterAll(this);
			permissions = null;
		}
	}

	public void registerListeners() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void registerOnlinePlayers() {
		permissions.removeAllPermissions();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.isOp()) {
				permissions.addOpPermissions(p);
			} else {
				permissions.addDefaultPermissions(p);
			}
		}
	}

	public class implementPermissions implements Runnable {
		Player p;

		implementPermissions(Player p) {
			this.p = p;
		}

		public void run() {
			if (p.isOp()) {
				permissions.removePermissions(p.getUniqueId());
				permissions.addOpPermissions(p);
			} else {
				permissions.removePermissions(p.getUniqueId());
				permissions.addDefaultPermissions(p);
			}
			tasks.remove(p.getName());
		}
	}

	public int syncImplementPermissions(Player p) {
		return Bukkit.getServer().getScheduler()
				.scheduleSyncDelayedTask(plugin, new implementPermissions(p));
	}

	public class opPermissionsCheck implements Runnable {
		Player p;
		boolean wasOp;

		opPermissionsCheck(Player p, boolean wasOp) {
			this.p = p;
			this.wasOp = wasOp;
		}

		public void run() {
			if (wasOp) {
				if (!p.isOp()) {
					permissions.removePermissions(p.getUniqueId());
					permissions.addDefaultPermissions(p);
				}
			} else {
				if (p.isOp()) {
					permissions.addOpPermissions(p);
				}
			}
			tasks.remove(p.getUniqueId());
		}
	}

	public int syncOpPermissionsCheck(Player p, boolean wasOp) {
		return Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new opPermissionsCheck(p, wasOp));
	}

	public void stopAllPendingTasks() {
		for (UUID uuid : tasks.keySet()) {
			Bukkit.getScheduler().cancelTask(tasks.remove(uuid));
		}
	}
}
