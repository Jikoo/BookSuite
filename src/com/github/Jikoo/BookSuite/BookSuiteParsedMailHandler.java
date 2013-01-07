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
		
		// adam do this part :D
	}
}
