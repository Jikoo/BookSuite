package com.github.Jikoo.BookSuite;

import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookSuiteMailExecutor {
	
BookSuite plugin;
Player p;
String to;
String title;
String itemMetaName;
BookMeta bm;
	
	public BookSuiteMailExecutor(BookSuite plugin, Player p){
		this.plugin = plugin;
		this.p = p;
	}



	public void sendMail(){
		p.sendMessage(ChatColor.DARK_RED+"This function does not yet exist.");
		getSendingData();
		parseSendingData();
		BookMeta newBook = (BookMeta) new ItemStack(Material.WRITTEN_BOOK).getItemMeta();
		newBook.setAuthor(p.getName());
		for (int i=1; i<bm.getPageCount(); i++)
			newBook.addPage(bm.getPage(i));
		if (itemMetaName!=""){
			newBook.setTitle("Package: "+title);
			newBook.addPage("Attached: "+itemMetaName);
			Inventory inv = p.getInventory();
			for (ItemStack is:inv.getContents())
				if (is.hasItemMeta())
					if(is.getItemMeta().hasDisplayName())
						if (is.getItemMeta().getDisplayName().equalsIgnoreCase(itemMetaName)){
							BookSuiteFileManager.makeFileFromItemStack(is, plugin.getDataFolder()+"/Mail/"+to+"/Items/", itemMetaName);
							inv.remove(is);
						}
		} else newBook.setTitle(title);
		BookSuiteFileManager.makeFileFromBookMeta(newBook, plugin.getDataFolder()+"/Mail/"+to+"/Books/", title);
		//TODO append index.bsm
		
		
		
		
		
	}
	
	
	
	public static Inventory getMail(Player p, BookSuite plugin){
		Inventory mailbox =  Bukkit.createInventory(p, 2, "MailBox");
		Scanner s = new Scanner("/BookSuite/Mail/index.bsm");
		while (s.hasNext()){
			if (mailbox.firstEmpty()!=-1){
				ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
				is.setItemMeta(BookSuiteFileManager.makeBookMetaFromText(s.nextLine(), plugin.getDataFolder()+"/Mail/"+p.getName()+"/Books/", ""));
				mailbox.addItem(is);
			}
			
		}
		return mailbox;
	}




	public void getSendingData(){
		bm = (BookMeta) p.getItemInHand().getItemMeta();
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
