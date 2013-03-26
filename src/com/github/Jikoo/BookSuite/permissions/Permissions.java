package com.github.Jikoo.BookSuite.permissions;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class Permissions {
	
	boolean enabled = false;
	HashMap<String, PermissionAttachment> attachments = new HashMap<String, PermissionAttachment>();
	
	public void addDefaultPermissions(Player p){
		if(!attachments.containsKey(p.getName())){
			attachments.put(p.getName(), p.addAttachment(Bukkit.getPluginManager().getPlugin("BookSuite")));
		}
		attachments.get(p.getName()).setPermission("booksuite.standard", true);
	}
	
	
	public void addOpPermissions(Player p){
		if(!attachments.containsKey(p.getName())){
			attachments.put(p.getName(), p.addAttachment(Bukkit.getPluginManager().getPlugin("BookSuite")));
		}
		attachments.get(p.getName()).setPermission("booksuite.admin", true);
	}
	
	public void removePermissions(Player p){
			attachments.remove(p.getName()).remove();
	}
	
	
	public void removeAllPermissions(){
		for(String pName:attachments.keySet()){
			attachments.remove(pName).remove();
		}
	}
}
