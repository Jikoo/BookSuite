package com.github.Jikoo.BookSuite;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookSuiteMailHandler {
	
	BookSuite plugin;
	String recipient;
	String attachment;
	int quantity;
	String missingAttachments;
	Player p;
	
	BookSuiteMailHandler(BookSuite plugin, Player p){
		this.plugin = plugin;
		this.p = p;
	}
	
	
	public void sendMail(BookMeta bm){
		p.sendMessage(ChatColor.DARK_RED+"This function does not yet exist.");
		if(true)//Yes, this is awful. It does fix the unreachable code warning that really annoys me though.
			return;
		ItemStack is = new ItemStack(Material.WRITTEN_BOOK, 1);
		is.setItemMeta(parseBookData(bm));
		if(is.getItemMeta()==null){
			p.sendMessage(ChatColor.DARK_RED+"Error with sending format");
			return;
		}
		
		//comment block is for reference, need to rewrite this all.
		//issue currently is saving/loading mail - what happens once mail is loaded into a chest?
		//What happens to mail left in a chest? thinking rewrite to file
		/*
		Chest c = (Chest) b;
		Inventory boxInv = c.getBlockInventory();
		Inventory senderInv = p.getInventory();
		
		if(attachment!=null)
			if(!hasAttachment(senderInv)){
				p.sendMessage(ChatColor.DARK_RED+"You do not have the specified attachments!");
				return;
			}
			
		if (boxHasSpace(boxInv)){
			senderInv.removeItem(p.getItemInHand());
			boxInv.addItem(is);
			if (attachment!=null){
				senderInv.removeItem(new ItemStack(Material.matchMaterial(attachment), quantity));//TODO try/catch parseInt(attachment) to support itemId
				boxInv.addItem(new ItemStack(Material.matchMaterial(attachment), quantity));
			}
			
		}
		else
			p.sendMessage(ChatColor.DARK_RED+recipient+"'s mailbox is too full!");*/
	}
	
	public boolean hasAttachment(Inventory inv){
		
		try {
			if(inv.contains(new ItemStack(Material.matchMaterial(attachment), quantity)))
				return true;
		} catch (Exception e) {
			p.sendMessage("Error: "+attachment+" is not a valid item name. Acceptable names: http://jd.bukkit.org/doxygen/d6/d0e/enumorg_1_1bukkit_1_1Material.html");
		}
		return false;
	}
	
	public boolean boxHasSpace(Inventory inv){
		if(attachment==null)
			return (inv.firstEmpty()!=-1);
		int freeSpaces=0;
		for(int i=0;i<27;i++)
			if(inv.getItem(i)==null)
				freeSpaces++;
		return (freeSpaces>=2);
	}
	
	
	
	public BookMeta parseBookData(BookMeta book){
		if(!book.hasPages())
			return null;
		BookMeta newBook = (BookMeta) new ItemStack(Material.WRITTEN_BOOK, 1).getItemMeta();
		newBook.setAuthor(book.getAuthor());
		newBook.setTitle(book.getTitle());
		List<String> newPages = book.getPages();
		//this could probably be done more easily but I can't seem to remove newPages[0]
		int i = 0;
		for(String s:newPages){
			if(i != 0) newBook.addPage(s);
			i++;
		}
		String firstPage = book.getPage(1).toLowerCase();
		firstPage.replaceFirst("\\A.*to:\\s*", "");
		firstPage.replaceAll("(attachment:)|(attach:)|(att:)", " ");
		firstPage.replaceAll("(quantity:)|(q:)|(#:)", " ");
		firstPage.replaceAll("\\W", "\n");
		firstPage.replaceFirst("\n+", "\n");
		firstPage.replaceAll("\n$", "");
		String[] pageLines = firstPage.split("\n");
		
			recipient = pageLines[0];
			try {
				attachment = pageLines[1];
				try {
					quantity = Integer.parseInt(pageLines[3]);
				} catch (Exception e) {
					quantity = 1;
				}
			} catch (Exception e) {
				attachment = null;
			}
			
		return newBook;
	}
}
