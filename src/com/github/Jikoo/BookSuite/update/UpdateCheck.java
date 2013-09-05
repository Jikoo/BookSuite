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
package com.github.Jikoo.BookSuite.update;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.Jikoo.BookSuite.BSLogger;
import com.github.Jikoo.BookSuite.BookSuite;

public class UpdateCheck implements Listener {

	BookSuite plugin = BookSuite.getInstance();
	String update;
	int current;
	boolean enabled = false;

	public boolean checkForUpdates() {
		try {
			URL feed = new URL(
					"http://dev.bukkit.org/bukkit-plugins/booksuite/files.rss");
			Scanner stream = new Scanner(feed.openStream());
			boolean item = false;
			while (stream.hasNextLine()) {
				String line = stream.nextLine();
				if (line.contains("<item>")) {
					item = true;
				}
				if (item) {
					if (line.contains("<title>")) {
						update = ChatColor.DARK_PURPLE
								+ line.replaceAll("(\\s)*<title>", "")
										.replaceAll("</title>(\\s)*", "");
						update += ChatColor.DARK_GREEN
								+ " is now available at ";
					}
					if (line.contains("<link>")) {
						line = line.replace("<link>", "")
								.replace("</link>", "").replaceAll("\\s", "");
						current = parseFileNumber(line);
						update += ChatColor.DARK_BLUE + line;
						break;
					}
				}
			}
			stream.close();
			if (current > plugin.currentFile) {
				plugin.hasUpdate = true;
				plugin.updateString = update;
				return true;
			}
		} catch (MalformedURLException e) {
			BSLogger.warn("Error with update URL.");
			BSLogger.err(e);
		} catch (IOException e) {
			BSLogger.warn("Error checking for updates!");
			BSLogger.err(e);
		}
		return false;
	}

	public void enableNotifications() {
		if (!enabled) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
			enabled = true;
		}
	}

	public void disableNotifications() {
		if (enabled) {
			HandlerList.unregisterAll(this);
			enabled = false;
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getUpdate() {
		return update;
	}

	public int parseFileNumber(String s) {
		s = s.replaceAll("http://(.)*/booksuite/files/(\\d+)-(.?)*\\z", "$2");
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			BSLogger.warn("File check parsing error! Please report this!");
			BSLogger.warn("Relevant information: \"" + s + "\"");
			return 0;
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (event.getPlayer().hasPermission("booksuite.command.update")) {
			delayUpdateCheck(event.getPlayer(), false, 20L);
		}
	}

	public void delayUpdateCheck(CommandSender sender, boolean warn, Long length) {
		Bukkit.getServer()
				.getScheduler()
				.scheduleSyncDelayedTask(plugin,
						new startUpdateCheck(sender, warn), length);
	}

	public class startUpdateCheck implements Runnable {
		CommandSender sender;
		boolean warn;

		startUpdateCheck(CommandSender sender, boolean warn) {
			this.sender = sender;
			this.warn = warn;
		}

		public void run() {
			if (plugin.hasUpdate) {
				sender.sendMessage(plugin.updateString);
			} else {
				if (sender instanceof Player) {
					asyncUpdateCheck(sender.getName(), false);
				} else {
					asyncUpdateCheck(null, false);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void asyncUpdateCheck(String pName, boolean inform) {
		Bukkit.getServer()
				.getScheduler()
				.scheduleAsyncDelayedTask(plugin,
						new doUpdateCheck(pName, inform));
	}

	public class doUpdateCheck implements Runnable {
		String pName;
		boolean inform;

		doUpdateCheck(String pName, boolean inform) {
			this.pName = pName;
			this.inform = inform;
		}

		public void run() {
			if (checkForUpdates())
				syncUpdateCheck(pName, true, inform);
			else
				syncUpdateCheck(pName, false, inform);
		}
	}

	public void syncUpdateCheck(String pName, boolean hasUpdate, boolean inform) {
		Bukkit.getServer()
				.getScheduler()
				.scheduleSyncDelayedTask(plugin,
						new informUpdate(pName, hasUpdate, inform));
	}

	public class informUpdate implements Runnable {
		String pName;
		boolean hasUpdate;
		boolean inform;

		informUpdate(String pName, boolean hasUpdate, boolean inform) {
			this.pName = pName;
			this.hasUpdate = hasUpdate;
			this.inform = inform;
		}

		public void run() {
			if (pName != null) {
				if (Bukkit.getPlayerExact(pName) != null) {
					if (hasUpdate)
						Bukkit.getPlayerExact(pName).sendMessage(update);
					else if (inform)
						Bukkit.getPlayerExact(pName).sendMessage(
								ChatColor.DARK_GREEN
										+ "BookSuite is up to date!");
				}
			} else {
				if (hasUpdate)
					plugin.getServer().getConsoleSender().sendMessage(update);
				else if (inform)
					plugin.getServer().getConsoleSender().sendMessage(
							ChatColor.DARK_GREEN + "BookSuite is up to date!");
			}
		}
	}
}
