package com.github.Jikoo.BookSuite;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
		if (args.length != 1){
			sender.sendMessage(ChatColor.DARK_RED+"the proper use of this command is "+ChatColor.AQUA+"/makebook {url}");
			return false;
		}
		
		Player p = (Player) sender;
		
		if (plugin.usePermissions && !p.hasPermission("booksuite.command.makebook")){
			sender.sendMessage(ChatColor.DARK_RED+"you do not have permission to make books from file");
			return false;
		}
		
		if (!plugin.canObtainBook(p)){
			return false;
		}
		
		ItemStack newbook = new ItemStack(Material.WRITTEN_BOOK, 1);
		
		
		
		
		
		
		newbook.setItemMeta(BookSuiteFileManager.makeBookMetaData(args[0], p));
		
		p.getInventory().addItem(newbook);
		return true;
	}
	
	
}
