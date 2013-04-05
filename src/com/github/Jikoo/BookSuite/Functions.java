package com.github.Jikoo.BookSuite;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Functions {
	
	
	
	/**
	 * master method for checking if the player can obtain the books
	 *
	 * @param p the player attempting to obtain the book
	 * @return whether the player can obtain the book
	 */
	public boolean canObtainBook(Player p){
		Inventory inv = p.getInventory();
		
		if (p.hasPermission("booksuite.book.free") || p.getGameMode().equals(GameMode.CREATIVE)){
			if (inv.firstEmpty()==-1){
				p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
				return false;
			}
			return true;
		}
		String supplies = checkBookSupplies(inv);
		if (supplies.equals("crafted")){
			inv.removeItem(new ItemStack(Material.INK_SACK, 1));
			inv.removeItem(new ItemStack(Material.BOOK, 1));
			if (inv.firstEmpty()==-1){
				p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
				inv.addItem(new ItemStack(Material.INK_SACK, 1));
				inv.addItem(new ItemStack(Material.BOOK, 1));
				return false;
			}
			return true;
		}
		if (supplies.equals("uncrafted")){
			inv.removeItem(new ItemStack(Material.INK_SACK, 1));
			inv.removeItem(new ItemStack(Material.PAPER, 3));
			inv.removeItem(new ItemStack(Material.LEATHER, 1));
			if (inv.firstEmpty()==-1){
				p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
				inv.addItem(new ItemStack(Material.INK_SACK, 1));
				inv.removeItem(new ItemStack(Material.PAPER, 3));
				inv.removeItem(new ItemStack(Material.LEATHER, 1));
				return false;
			}
			return true;
		}
		p.sendMessage(ChatColor.DARK_RED+"To create a book, you need "+supplies+".");
		return false;
	}
	
	
	
	
	
	/**
	 * checks if the player has the supplies needed
	 * 
	 * @param inv the inventory of the player
	 * @return whether the player has the supplies needed to copy the book
	 */
	public String checkBookSupplies(Inventory inv){
		if (inv.contains(Material.BOOK) && inv.contains(Material.INK_SACK)){
			return "crafted";
		}
		else if (inv.contains(Material.INK_SACK)){
			if(inv.contains(new ItemStack(Material.PAPER, 3))&&inv.contains(Material.LEATHER)){
				return "uncrafted";
			}
			return "a book";
		}
		else if (inv.contains(Material.BOOK)){
			return "an ink sack";
		}
		return "a book and an ink sack";
	}
	
	
	
	
	
	public boolean unsign(Player p){
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
	
	
	
	public boolean setAuthor(Player p, String newAuthor){
		ItemStack book = p.getItemInHand();
		if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK))
			return false;
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setAuthor(newAuthor);
		book.setItemMeta(bookMeta);
		return true;
	}
	
	
	public boolean setTitle(Player p, String newTitle){
		if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK))
			return false;
		ItemStack book = p.getItemInHand();
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setTitle(newTitle);
		book.setItemMeta(bookMeta);
		return true;
	}
	
	
	public boolean insertPageAt(Player p, String pageNumber, String text){
		if(!p.getItemInHand().getType().equals(Material.BOOK_AND_QUILL)){
			p.sendMessage(ChatColor.DARK_RED+"You must be holding a book and quill to use this command!");
			return false;
		}
		ItemStack book = p.getItemInHand();
		BookMeta bm = (BookMeta)book.getItemMeta();
		List<String> pages = bm.getPages();
		try {
			int page = Integer.parseInt(pageNumber);
			pages.add(page-1, text);
		} catch (NumberFormatException e1) {
			p.sendMessage(ChatColor.DARK_RED+"Correct usage is \"/book addpage <page number> [optional page text]\"");
			return false;
		} catch (IndexOutOfBoundsException e) {
			p.sendMessage(ChatColor.DARK_RED+"Please enter a number between 1 and "+(bm.getPageCount()-1)+".");
			return false;
		}
		bm.setPages(pages);
		book.setItemMeta(bm);
		return true;
	}
	
	public boolean deletePageAt(Player p, String pageNumber){
		if (p.getItemInHand().getType().equals(Material.BOOK_AND_QUILL)){
			p.sendMessage(ChatColor.DARK_RED+"You must be holding a book and quill to use this command!");
			return false;
		}
		ItemStack book = p.getItemInHand();
		BookMeta bm = (BookMeta)book.getItemMeta();
		List<String> pages = bm.getPages();
		try {
			int page = Integer.parseInt(pageNumber);
			pages.remove(page-1);
		} catch (NumberFormatException e1) {
			p.sendMessage(ChatColor.DARK_RED+"Correct usage is \"/book delpage <page number>\"");
			return false;
		} catch (IndexOutOfBoundsException e) {
			p.sendMessage(ChatColor.DARK_RED+"Please enter a number between 1 and "+(bm.getPageCount()-1)+".");
			return false;
		}
		bm.setPages(pages);
		book.setItemMeta(bm);
		
		return true;
	}



	public boolean canObtainMap(Player p) {
		if(p.hasPermission("booksuite.copy.map")){
			Inventory inv = p.getInventory();
			if (p.hasPermission("booksuite.book.free") || p.getGameMode().equals(GameMode.CREATIVE)){
				if (inv.firstEmpty()==-1){
					p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
					return false;
				} else return true;
			} else if(inv.contains(new ItemStack(Material.PAPER, 9))){
				inv.remove(new ItemStack(Material.PAPER, 9));
				if(inv.firstEmpty()==-1){
					inv.addItem(new ItemStack(Material.PAPER, 9));
					p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
					return false;
				} else return true;
			} else {
				p.sendMessage(ChatColor.DARK_RED+"You need 9 paper to copy a map.");
				return false;
			}
		} else {
			p.sendMessage(ChatColor.DARK_RED+"You do not have permission to copy maps.");
			return false;
		}
	}
}
