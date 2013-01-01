package com.github.Jikoo.BookSuite;

import java.net.URL;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookSuiteCommandExecutor implements CommandExecutor{
	
	BookSuite plugin;
	
	public BookSuiteCommandExecutor(BookSuite plugin){
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (! (sender instanceof Player) ){
			sender.sendMessage(ChatColor.DARK_RED+"you must be a player to use this command");
			return false;
		}
		if (args.length != 2){
			sender.sendMessage(ChatColor.DARK_RED+"the proper use of this command is "+ChatColor.AQUA+"/makebook {title} {url}");
			return false;
		}
		
		Player p = (Player) sender;
		
		if (!p.hasPermission("booksuite.makebook")){
			sender.sendMessage(ChatColor.DARK_RED+"you do not have permission to make books from file");
			return false;
		}
		
		ItemStack newbook = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta text = (BookMeta)newbook.getItemMeta();
		
		text.setTitle(args[0]);
		text.setAuthor(p.getName());
		
		try {
			URL u = new URL(args[1]);
			Scanner s = new Scanner(u.openStream());
			while(s.hasNext()){
				String line = this.parseText(s.nextLine());
				text.addPage(line);
			}
		}
		catch(Exception ex) {}
		
		
		
		
		
		
		newbook.setItemMeta(text);
		
		p.getInventory().addItem(newbook);
		return true;
	}
	
	public String parseText(String text){
		text = text.replaceAll("<i>", "§o");
		text = text.replaceAll("<b>", "§l");
		text = text.replaceAll("<u>", "§n");
		text = text.replaceAll("<->", "§m");
		
		text = text.replaceAll("<black>", "§0");
		text = text.replaceAll("<darkblue>", "§1");
		text = text.replaceAll("<darkgreen>", "§2");
		text = text.replaceAll("<darkaqua>", "§3");
		text = text.replaceAll("<darkred>", "§4");
		text = text.replaceAll("<purple>", "§5");
		text = text.replaceAll("<gold>", "§6");
		text = text.replaceAll("<grey>", "§7");
		text = text.replaceAll("<darkgrey>", "§8");
		text = text.replaceAll("<indigo>", "§9");
		text = text.replaceAll("<brightgreen>", "§a");
		text = text.replaceAll("<aqua>", "§b");
		text = text.replaceAll("<red>", "§c");
		text = text.replaceAll("<pink>", "§d");
		text = text.replaceAll("<yellow>", "§e");
		text = text.replaceAll("<white>", "§f");
		
		text = text.replaceAll("</i>", "§r");
		text = text.replaceAll("</b>", "§r");
		text = text.replaceAll("</u>", "§r");
		text = text.replaceAll("</->", "§r");
		text = text.replaceAll("</color>", "§r");
		
		text = text.replaceAll("<n>", "\n");
		
		
		return text;
	}
}
