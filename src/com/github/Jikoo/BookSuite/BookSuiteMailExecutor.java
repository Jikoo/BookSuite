package com.github.Jikoo.BookSuite;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.BookMeta;

public class BookSuiteMailExecutor {
	
BookSuite plugin;
Player p;
String to;
String title;
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




	public void getSendingData(){
		BookMeta bm = (BookMeta) p.getItemInHand();//This is already guaranteed to be a written book by event handler
		if (bm.getTitle().equalsIgnoreCase("package")){
			String[] pageData = bm.getPage(0).split("\n");
			title = pageData[0];
			to = pageData[1];
			itemMetaName = pageData[2];
			
		}
	}
	
	public void parseSendingData(){
		title.replaceFirst("\\A.*([Pp]ackage|[Tt]itle):\\s*", "");
		title.replaceAll("\\W", "");
		to.replaceFirst("\\A.*[Tt]o:\\s*", "");
		to.replaceAll("\\W", "");
		itemMetaName.replaceFirst("\\A.*[Ii]tem:\\s*", "");
	}
}
