package com.github.Jikoo.BookSuite;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookSuiteParsedMailHandler {
	BookMeta bm;
	String[] recipientNames;
	ItemStack[] specifiedItemMeta;
	
	public BookSuiteParsedMailHandler(BookMeta bm){
		this.bm = bm;
		this.parse(bm);
	}
	
	
	public String[] getRecipients(){
		return null;
	}
	
	
	public ItemStack[] getMaterials(){
		return null;
	}
	
	public ItemStack[] getAdjustedItemStack(){
		return null;
	}
	
	
	public void parse(BookMeta bm){
		String recps = bm.getPage(0);
		String items = bm.getPage(1);
		
		
		recps.replaceFirst("\\A.*\\b", "");//consume start of page up to first word characters
		recps.replaceAll("to:\\s*", "");//if "to:" consume that+ following spaces
		recps.replaceAll("\\W", "\n");//all non-word characters (a-zA-Z_0-9) replaced with \n
		recps.replaceFirst("\n+", "\n");//consume repeat \n
		recps.replaceAll("\n$", "");//consume \n at end
		recipientNames = recps.split("\n");
		
		/*
		 * Probably going to allow only 1 item attachment initially - handling multiples would get WAY too complex with metadata
		 * 	(the general idea is that this will be used to give gifts, not necessarily to transport supplies.. TODO configurable max itemstack)
		 * 
		 * not sure that this is how I want to do items, may end up making it require different items to be
		 * 	written in a specific format (attachment[0-9]*:\\s*item\\s*:\\s*data\\s* - obviously need a couple regex for this, but an idea)
		 * Problem really is handling item meta - do we make them specify, or do we check inventory
		 * 	for first acceptable and take it, copy the meta off of it in the item to file process?
		 * 	I assume the first option is better (or: another page per item, specify attach number in
		 * 	first page?) - imagine accidentally mailing off your heavily enchanted diamond sword by accident.
		 * 
		 */
		items.replaceFirst("\\A.*\\b", "");//consume start of page up to first word characters
		items.replaceAll("[\\W&&[^:]", "\n");//all non-word characters except colon (a-zA-Z_0-9:) replaced with \n
		items.replaceFirst("\n+", "\n");//consume repeat \n
		items.replaceAll("\n$", "");//consume \n at end
	}
}
