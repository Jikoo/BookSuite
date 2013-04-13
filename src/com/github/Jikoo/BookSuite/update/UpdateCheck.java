package com.github.Jikoo.BookSuite.update;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.Jikoo.BookSuite.BookSuite;

public class UpdateCheck implements Listener {

	BookSuite plugin;
	String update;
	int current;
	boolean enabled = false;

	public UpdateCheck(BookSuite plugin) {
		this.plugin = plugin;
	}

	public boolean checkForUpdates() {
		try {
			URL feed = new URL("http://dev.bukkit.org/server-mods/booksuite/files.rss");
			Scanner stream = new Scanner(feed.openStream());
			boolean item = false;
			while (stream.hasNextLine()) {
				String line = stream.nextLine();
				if (line.contains("<item>")) {
					item = true;
				}
				if (item) {
					if (line.contains("<title>")) {
						update = ChatColor.DARK_PURPLE + line.replaceAll("(\\s)*<title>", "").replaceAll("</title>(\\s)*", "");
						update += ChatColor.DARK_GREEN + " is now available at ";
					}
					if (line.contains("<link>")) {
						line = line.replace("<link>", "").replace("</link>", "").replaceAll("\\s", "");
						current = parseFileNumber(line);
						update += ChatColor.DARK_BLUE + line;
						break;
					}
				}
			}
			stream.close();
			if (current > plugin.currentFile)
				return true;
		} catch (MalformedURLException e) {
			plugin.getLogger().warning("[BookSuite] Error with update URL: " + e);
			e.printStackTrace();
			plugin.getLogger().warning("[BookSuite] End error report. Please report this error!");
		} catch (IOException e) {
			plugin.getLogger().warning("[BookSuite] Error checking for updates!");
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
		s = s.replace("http://dev.bukkit.org/server-mods/booksuite/files/", "");
		s = s.replaceAll("-.*", "");
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			plugin.getLogger().warning("[BookSuite] File check parsing error! Please report this error!");
			return 0;
		}
	}
	
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		delayUpdateCheck(event.getPlayer());
	}

	public void delayUpdateCheck(Player p) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new startUpdateCheck(p), 20L);
	}

	public class startUpdateCheck implements Runnable {
		Player p;
		startUpdateCheck(Player p) {
			this.p = p;
		}
		public void run() {
			if (p.hasPermission("booksuite.command.update"))
				asyncUpdateCheck(p.getName(), false);
		}
	}

	public void asyncUpdateCheck(String pName, boolean inform) {
		Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new doUpdateCheck(pName, inform));
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
			else syncUpdateCheck(pName, false, inform);
		}
	}

	public void syncUpdateCheck(String pName, boolean hasUpdate, boolean inform) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new informUpdate(pName, hasUpdate, inform));
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
					if (hasUpdate) Bukkit.getPlayerExact(pName).sendMessage(update);
					else if (inform) Bukkit.getPlayerExact(pName).sendMessage(ChatColor.DARK_GREEN+"BookSuite is up to date!");
				}
			} else {
				if(hasUpdate) plugin.getLogger().info(update);
				else if (inform) plugin.getLogger().info(ChatColor.DARK_GREEN+"BookSuite is up to date!");
			}
		}
	}
}
