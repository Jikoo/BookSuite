package com.github.Jikoo.BookSuite.permissions;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.github.Jikoo.BookSuite.BookSuite;

public class PermissionsListener implements Listener, CommandExecutor{
	Permissions permissions = new Permissions();
	boolean enabled = false;
	BookSuite plugin;
	
	//Events: join, quit, kick, op, deop
	public PermissionsListener(BookSuite plugin){
		this.plugin=plugin;
	}
	
	
	@EventHandler
	public void onLogin(PlayerJoinEvent event){
		if(!enabled)
			return;
		if(event.getPlayer().isOp()){
			permissions.addOpPermissions(event.getPlayer());
		} else {
			permissions.addDefaultPermissions(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event){
		if(enabled)
			permissions.removePermissions(event.getPlayer());
	}
	
	
	@EventHandler
	public void onKick(PlayerKickEvent event){
		if(enabled)
			permissions.removePermissions(event.getPlayer());
	}
	
	
	
	public boolean isEnabled(){
		return enabled;
	}
	
	public void enable(){
		enabled=true;
		for(Player p : Bukkit.getOnlinePlayers()){
			if(p.isOp()){
				permissions.addOpPermissions(p);
			} else {
				permissions.addDefaultPermissions(p);
			}
		}
	}
	
	public void disable(){
		enabled=false;
		permissions.removeAllPermissions();
		HandlerList.unregisterAll(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length!=1)
			return true;
		if(cmd.getName().equals("op")){
			if(!Bukkit.getPlayerExact(args[0]).isOp()){
				
			}
		}
		return true;
	}
	
	public class opPermissionsCheck implements Runnable{
		Player p;
		boolean wasOp;
		opPermissionsCheck(Player p, boolean wasOp){
			this.p=p;
		}
		public void run() {
			if(wasOp){
				if(!p.isOp()){
					permissions.removePermissions(p);
					permissions.addDefaultPermissions(p);
				}
			} else {
				if(p.isOp()){
					permissions.addOpPermissions(p);
				}
			}
		}
	}
	
	public void syncOpPermissionsCheck(Player p, boolean wasOp){
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new opPermissionsCheck(p, wasOp), 0L);
	}
}
