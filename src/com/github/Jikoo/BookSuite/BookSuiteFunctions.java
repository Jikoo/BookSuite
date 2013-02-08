package com.github.Jikoo.BookSuite;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookSuiteFunctions {
	
	public static boolean unsign(Player p){
		ItemStack unsign = p.getItemInHand();
		if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK))
			return false;
		BookMeta unsignMeta = (BookMeta) unsign.getItemMeta();
		unsignMeta.setAuthor(null);
		unsignMeta.setTitle(null);
		unsign.setItemMeta(unsignMeta);
		unsign.setType(Material.BOOK_AND_QUILL);
		return true;
	}
	
	
	
	public static boolean setAuthor(Player p, String newAuthor){
		ItemStack book = p.getItemInHand();
		if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK))
			return false;
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setAuthor(newAuthor);
		book.setItemMeta(bookMeta);
		return true;
	}
	
	
	public static boolean setTitle(Player p, String newTitle){
		ItemStack book = p.getItemInHand();
		if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK))
			return false;
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setTitle(newTitle);
		book.setItemMeta(bookMeta);
		return true;
	}

}
