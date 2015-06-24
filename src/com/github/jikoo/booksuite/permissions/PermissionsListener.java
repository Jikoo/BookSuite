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
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jikoo.booksuite.BookSuite;

public class PermissionsListener implements Listener {
	Permissions permissions;
	boolean enabled = false;
	BookSuite plugin;

	public PermissionsListener(BookSuite plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
		new BukkitRunnable() {
			@Override
			public void run() {
				OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
				if (!player.isOnline()) {
					return;
				}
				if (player.isOp()) {
					permissions.addOpPermissions(player.getPlayer());
				} else {
					permissions.addDefaultPermissions(player.getPlayer());
				}
			}
		}.runTask(BookSuite.getInstance());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().toLowerCase().contains("op")) {
			String[] command = event.getMessage().toLowerCase().replaceAll("/", "").split(" ");
			if (!command[0].equals("op") && !command[0].equals("deop")) {
				return;
			}
			final Map<UUID, Boolean> players = new HashMap<UUID, Boolean>();
			if (command.length == 1) {
				players.put(event.getPlayer().getUniqueId(), event.getPlayer().isOp());
			} else {
				// Have to compensate for other plugins where vanilla would use exact player
				for (Player p : Bukkit.matchPlayer(command[1])) {
					players.put(p.getUniqueId(), p.isOp());
				}
			}
			if (players.isEmpty()) {
				return;
			}
			new BukkitRunnable() {
				@Override
				public void run() {
					for (Map.Entry<UUID, Boolean> entry : players.entrySet()) {
						OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
						if (!player.isOnline()) {
							continue;
						}
						if (entry.getValue() && !player.isOp()) {
							permissions.removePermissions(entry.getKey());
							permissions.addDefaultPermissions(player.getPlayer());
							continue;
						}
						if (!entry.getValue() && player.isOp()) {
							permissions.addOpPermissions(player.getPlayer());
						}
					}
				}
			}.runTask(BookSuite.getInstance());
		}
	}

	@EventHandler
	public void onConsoleCommand(ServerCommandEvent event) {
		if (event.getCommand().toLowerCase().contains("op")) {
			String[] command = event.getCommand().toLowerCase().split(" ");
			if (command.length == 2 && (command[0].equals("op") || command[0].equals("deop"))) {
				final Map<UUID, Boolean> players = new HashMap<UUID, Boolean>();
				// Have to compensate for other plugins where vanilla would use exact player
				for (Player p : Bukkit.matchPlayer(command[1])) {
					players.put(p.getUniqueId(), p.isOp());
				}
				if (players.isEmpty()) {
					return;
				}
				new BukkitRunnable() {
					@Override
					public void run() {
						for (Map.Entry<UUID, Boolean> entry : players.entrySet()) {
							OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
							if (!player.isOnline()) {
								continue;
							}
							if (entry.getValue() && !player.isOp()) {
								permissions.removePermissions(entry.getKey());
								permissions.addDefaultPermissions(player.getPlayer());
								continue;
							}
							if (!entry.getValue() && player.isOp()) {
								permissions.addOpPermissions(player.getPlayer());
							}
						}
					}
				}.runTask(BookSuite.getInstance());
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
}
