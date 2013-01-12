package com.github.Jikoo.BookSuite;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

public class BookSuiteMailExecutor {
	
BookSuite plugin;
Player p;
String to;
String bookMessageName;
String itemMetaName;
	
	public BookSuiteMailExecutor(BookSuite plugin, Player p){
		this.plugin = plugin;
		this.p = p;
	}



	public void sendMail(){
		p.sendMessage(ChatColor.DARK_RED+"This function does not yet exist.");
		getSendingData();
		parseSendingData();
		//Verify inventory
		//Files!
	}



	//The whole point is that it's commandless >.> why have a packing slip or details in the book then?
	/*public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (! (sender instanceof Player) ){
			sender.sendMessage(ChatColor.DARK_RED+"you must be a player to use this command");
			return false;
		}
		*/
	public void getSendingData(){
		PlayerInventory i = p.getInventory();
		BookMeta bm = (BookMeta) p.getItemInHand();//This is already guaranteed to be a written book by event handler
		String title = bm.getTitle();
		title.replaceFirst("[\\W_]*", "");//(any non [a-zA-Z0-9] consumed)
		if (title.equalsIgnoreCase("packingslip")){
			to = bm.getPage(0);
			bookMessageName = bm.getPage(1);
			itemMetaName = bm.getPage(2);
		}
	}
	
	public void parseSendingData(){
		to.replaceFirst("\\A.*[Tt][Oo]:\\s*", "");
		to.replaceAll("\\W", "");
		//no clue what we want to do for the rest, but it can be handled in similar format.
	}
}
