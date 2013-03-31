package com.github.Jikoo.BookSuite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Alias {
	
	FileConfiguration aliasYML = new YamlConfiguration();
	String aliasType;
	BookSuite plugin;
	
	public Alias(BookSuite plugin){
		this.plugin=plugin;
		reload();
	}
	
	
	public void reload(){
		aliasType = plugin.getConfig().getString("alias-mode");
		if(aliasType.equals("multi")){
			File aliasFile = new File(plugin.getDataFolder(), "aliases.yml");
			if(aliasFile.exists()){
				aliasYML = YamlConfiguration.loadConfiguration(aliasFile);
			} else {
				try {
					aliasFile.createNewFile();
				} catch (IOException e) {
					plugin.getLogger().warning(ChatColor.DARK_RED+"Could not create aliases.yml!");
				}
			}
		}
	}
	
	
	public boolean addAlias(String pName, String newAlias){
		if(aliasYML.getStringList(pName).contains(newAlias))
			return false;
		aliasYML.set(pName, aliasYML.getStringList(pName).add(newAlias));
		return true;
	}
	
	
	public void addAliasToTarget(Player p, Player target, String newAlias, boolean warn){
		if(!aliasType.equals("multi")){
			p.sendMessage(ChatColor.DARK_RED+"Additional aliases are not allowed in the configuration. Please contact your server administrator.");
			return;
		}
		if(!addAlias(target.getName(), newAlias)){
			p.sendMessage(ChatColor.DARK_RED+target.getName()+" already has the alias "+newAlias);
			return;
		} else {
			p.sendMessage(ChatColor.DARK_GREEN+"Added alias \""+newAlias+"\" to "+target.getName());
			if(warn)
				target.sendMessage(ChatColor.DARK_GREEN+p.getName()+" added "+newAlias+" to your list of aliases!");
		}
	}
	
	
	
	public ArrayList<String> getAliases(Player p){
		ArrayList<String> aliases = new ArrayList<String>();
		aliases.add(p.getName());
		switch(aliasType){
			case "multi":
				aliases.add(p.getDisplayName());
				for(String s : aliasYML.getStringList(p.getName())){
					aliases.add(s);
				}
				break;
			case "default":
				aliases.add(p.getDisplayName());
				break;
			default: break;
		}
		return aliases;
	}

}
