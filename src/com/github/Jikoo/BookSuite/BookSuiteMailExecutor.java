package com.github.Jikoo.BookSuite;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

public class BookSuiteMailExecutor {
	
BookSuite plugin;
	
	public BookSuiteMailExecutor(BookSuite plugin){
		this.plugin = plugin;
	}
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (! (sender instanceof Player) ){
			sender.sendMessage(ChatColor.DARK_RED+"you must be a player to use this command");
			return false;
		}
		Player p = (Player) sender;
		PlayerInventory i = p.getInventory();
		ItemStack potentiallyABook = i.getItemInHand();
		if (potentiallyABook.equals(new ItemStack(Material.WRITTEN_BOOK, 1))){
			BookMeta bm = (BookMeta)(potentiallyABook.getItemMeta());
			if (bm.getTitle().equalsIgnoreCase("packing slip") || bm.getTitle().equalsIgnoreCase("packingslip")){
				String to = bm.getPage(0);
				String bookMessageName = bm.getPage(1);
				String itemMetaName = bm.getPage(2);
			}
		}
		
		
		
		
		return true;
	}
}
