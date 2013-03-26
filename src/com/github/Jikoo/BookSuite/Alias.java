package com.github.Jikoo.BookSuite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Alias {
	
	FileConfiguration aliasYML;
	String aliasType;
	BookSuite plugin;
	
	public Alias(BookSuite plugin){
		this.plugin=plugin;
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
	
	
	
	public void reloadAliases(){
		//do this when it isn't 3am ok
		
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
