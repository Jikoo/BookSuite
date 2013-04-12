package com.github.Jikoo.BookSuite.permissions;

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


	public PermissionsListener(BookSuite plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onLogin(PlayerJoinEvent event) {
		syncImplementPermissions(event.getPlayer());
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event){
		if(event.isCancelled())
			return;
		if(event.getMessage().toLowerCase().contains("op")){
			String[] command = event.getMessage().toLowerCase().split(" ");
			if (command.length==2){
				if (command[0].equals("/op") || command[0].equals("/deop")){
					//While vanilla would use getPlayerExact, we need to compensate for autofill from plugins
					Player p = Bukkit.getPlayer(command[1]);
					if (p != null)
						syncOpPermissionsCheck(p, p.isOp());
				}
			}
		}
	}

	@EventHandler
	public void onConsoleCommand(ServerCommandEvent event){
		if(event.getCommand().toLowerCase().contains("op")) {
			String[] command = event.getCommand().toLowerCase().split(" ");
			if (command.length == 2 && (command[0].equals("op") || command[0].equals("deop"))){
				Player p = Bukkit.getPlayer(command[1]);
				if (p != null)
					syncOpPermissionsCheck(p, p.isOp());
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		permissions.removePermissions(event.getPlayer().getName());
	}

	@EventHandler
	public void onKick(PlayerKickEvent event) {
		permissions.removePermissions(event.getPlayer().getName());
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



	public class implementPermissions implements Runnable {
		Player p;

		implementPermissions(Player p) {
			this.p = p;
		}

		public void run() {
			if (p.isOp()) {
				permissions.removePermissions(p.getName());
				permissions.addOpPermissions(p);
			} else {
				permissions.removePermissions(p.getName());
				permissions.addDefaultPermissions(p);
			}
		}
	}

	public void syncImplementPermissions(Player p) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new implementPermissions(p), 0L);
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
					permissions.removePermissions(p.getName());
					permissions.addDefaultPermissions(p);
				}
			} else {
				if (p.isOp()) {
					permissions.addOpPermissions(p);
				}
			}
		}
	}

	public void syncOpPermissionsCheck(Player p, boolean wasOp) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new opPermissionsCheck(p, wasOp), 1L);
	}
}