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
boolean mailHasItemAttached;
	
	public BookSuiteMailExecutor(BookSuite plugin, Player p){
		this.plugin = plugin;
		this.p = p;
	}



	public void sendMail(){
		bm = (BookMeta) p.getItemInHand().getItemMeta();
		if (p.getName()!=bm.getAuthor()&&!p.hasPermission("booksuite.mail.send.other"))
		parseSendingData();
		BookMeta newBook = (BookMeta) new ItemStack(Material.WRITTEN_BOOK).getItemMeta();
		newBook.setAuthor(bm.getAuthor());
		Inventory inv = p.getInventory();
		ItemStack removeThis = new ItemStack(Material.DIRT, 1);
		boolean playerHasItem = false;
		for (int i=1; i<bm.getPageCount(); i++)
			newBook.addPage(bm.getPage(i));
		if (itemMetaName!=""){
			newBook.setTitle("Package: "+title);
			newBook.addPage("Attached:\n"+itemMetaName);
			for (ItemStack is:inv.getContents())
				if (is.hasItemMeta())
					if(is.getItemMeta().hasDisplayName())
						if (is.getItemMeta().getDisplayName().equalsIgnoreCase(itemMetaName)){
							removeThis = is;
							playerHasItem = true;
						}
		} else newBook.setTitle(title);
		
		
		if (mailHasItemAttached && !playerHasItem){
			p.sendMessage(ChatColor.DARK_RED+"Error: no such named item, please check spelling.");
			ItemStack unsign = p.getItemInHand();
			BookMeta unsignMeta = (BookMeta) unsign.getItemMeta();
			unsignMeta.setAuthor(null);
			unsignMeta.setTitle(null);
			unsign.setItemMeta(unsignMeta);
			unsign.setType(Material.BOOK_AND_QUILL);
			return;
		}
		
		
		if(BookSuiteFileManager.appendMailIndex(plugin.getDataFolder()+"/Mail/"+to, title)){
			if (mailHasItemAttached && playerHasItem){
				BookSuiteFileManager.makeFileFromItemStack(removeThis, plugin.getDataFolder()+"/Mail/"+to+"/Items/", itemMetaName+".item");
				inv.remove(removeThis);
			}
			
			
			BookSuiteFileManager.makeFileFromBookMeta(newBook, plugin.getDataFolder()+"/Mail/"+to+"/Books/", title+".book");
			inv.remove(p.getItemInHand());
			p.sendMessage(ChatColor.DARK_GREEN+"Mail sent successfully!");
		} else p.sendMessage(ChatColor.DARK_RED+"Error writing mail index!");
	}
	
	
	
	public static Inventory getMailBoxInv(Player p, BookSuite plugin){
		Inventory mailbox =  Bukkit.createInventory(p, 2, p.getDisplayName()+"'s MailBox");
		Scanner s = new Scanner(plugin.getDataFolder()+"/Mail/index.bsm");
		while (s.hasNext()){
			if (mailbox.firstEmpty()!=-1){
				ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
				is.setItemMeta(BookSuiteFileManager.makeBookMetaFromText(s.nextLine()+".book", plugin.getDataFolder()+"/Mail/"+p.getName()+"/Books/", ""));
				mailbox.addItem(is);
			}
			
		}
		return mailbox;
	}




	public void parseSendingData(){
		if (bm.getTitle().equalsIgnoreCase("package")){
			String[] pageData = bm.getPage(0).split("\n");
			title = pageData[0];
			title.replaceFirst("\\A.*([Pp]ackage|[Tt]itle):\\s*", "");
			title.replaceAll("\\W", "");
			to = pageData[1];
			to.replaceFirst("\\A.*[Tt]o:\\s*", "");
			to.replaceAll("\\W", "");
			itemMetaName = pageData[2];
			if (itemMetaName!=null){
				itemMetaName.replaceFirst("\\A.*([Ii]tem|[Aa]ttach):\\s*", "");
				if(itemMetaName.equalsIgnoreCase("n/a")||itemMetaName.equalsIgnoreCase("none")||itemMetaName.equalsIgnoreCase("nothing"))
					itemMetaName="";
				if(itemMetaName!="") mailHasItemAttached = true;
			}
		}
	}
}