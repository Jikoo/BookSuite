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
boolean hasItemAttached;
	
	public BookSuiteMailExecutor(BookSuite plugin, Player p){
		this.plugin = plugin;
		this.p = p;
	}



	public void sendMail(){
		getSendingData();
		parseSendingData();
		BookMeta newBook = (BookMeta) new ItemStack(Material.WRITTEN_BOOK).getItemMeta();
		newBook.setAuthor(p.getName());
		Inventory inv = p.getInventory();
		boolean hasItemInInv = false;
		for (int i=1; i<bm.getPageCount(); i++)
			newBook.addPage(bm.getPage(i));
		if (itemMetaName!=""){
			newBook.setTitle("Package: "+title);
			newBook.addPage("Attached: "+itemMetaName);
			for (ItemStack is:inv.getContents())
				if (is.hasItemMeta())
					if(is.getItemMeta().hasDisplayName())
						if (is.getItemMeta().getDisplayName().equalsIgnoreCase(itemMetaName)){
							BookSuiteFileManager.makeFileFromItemStack(is, plugin.getDataFolder()+"/Mail/"+to+"/Items/", itemMetaName);
							inv.remove(is);
							hasItemInInv = true;
						}
		} else newBook.setTitle(title);
		if (hasItemAttached && !hasItemInInv){
			p.sendMessage(ChatColor.DARK_RED+"Error retrieving item, please check spelling.");
			ItemStack unsign = p.getItemInHand();
			BookMeta unsignMeta = (BookMeta) unsign.getItemMeta();
			unsignMeta.setAuthor(null);
			unsignMeta.setTitle(null);
			unsign.setItemMeta(unsignMeta);
			unsign.setType(Material.BOOK_AND_QUILL);
			return;
		}
		BookSuiteFileManager.makeFileFromBookMeta(newBook, plugin.getDataFolder()+"/Mail/"+to+"/Books/", title);
		inv.remove(p.getItemInHand());
		BookSuiteFileManager.appendMailIndex(plugin.getDataFolder()+"/Mail/"+to, title);
		p.sendMessage(ChatColor.DARK_GREEN+"Mail sent successfully!");
		
		
		
		
		
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
			if (pageData[2]!=null&&pageData[2]!="") hasItemAttached = true;
			
		}
	}
	
	public void parseSendingData(){
		title.replaceFirst("\\A.*([Pp]ackage|[Tt]itle):\\s*", "");
		title.replaceAll("\\W", "");
		to.replaceFirst("\\A.*[Tt]o:\\s*", "");
		to.replaceAll("\\W", "");
		itemMetaName.replaceFirst("\\A.*([Ii]tem|[Aa]ttach):\\s*", "");
		if(itemMetaName.equalsIgnoreCase("n/a")||itemMetaName.equalsIgnoreCase("none")||itemMetaName.equalsIgnoreCase("nothing"))
			itemMetaName="";
	}
}
