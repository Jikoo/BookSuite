/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn
 ******************************************************************************/
package com.github.Jikoo.BookSuite.copy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Copier {
	HashMap<Integer, ? extends ItemStack> inkMap;
	HashMap<Integer, ? extends ItemStack> bookMap;
	HashMap<Integer, ? extends ItemStack> paperMap;
	HashMap<Integer, ? extends ItemStack> leatherMap;
	//HashMap<Integer, ? extends ItemStack> blankbaqMap;TODO OR NOTTODO
	HashMap<Integer, ? extends ItemStack> spaceMap;
	int removedPaper;
	int removedBooks;
	
	ArrayList<ItemStack> consumedSupplies;
	
	int maxUncraftedBook;
	
	Player p;
	int copies;
	
	public Copier(Player p, int copies) {
		this.p = p;
		this.copies = copies;
	}
	
	
	public CopyFailureReason copy() {
		
		switch (p.getItemInHand().getType()) {
		case WRITTEN_BOOK:
			if (!p.hasPermission("booksuite.copy.self"))
				return CopyFailureReason.PERMISSION;
			break;
		case BOOK_AND_QUILL:
			if (!p.hasPermission("booksuite.copy.unsigned"))
				return CopyFailureReason.PERMISSION;
			break;
		case MAP:
			if (!p.hasPermission("booksuite.copy.map"))
				return CopyFailureReason.PERMISSION;
			break;
		default: return CopyFailureReason.UNCOPIABLE;
		}
		
		
		if (!p.hasPermission("booksuite.copy.free")) {
			int maxPossible = getMaximumCopiables(p);
			if (copies > maxPossible) {
				p.sendMessage(ChatColor.DARK_RED + "You only have the supplies to make " + maxPossible + " copies.");
				copies = maxPossible;
			}
			
			
			//remove supplies
		}
		
		//check free space
		
		/*
		 * Before I forget:
		 * 
		 * 
		 * DO FOR NEATNESS
		 * TODO check remove stack of 64 auto-removing next
		 * --won't work.
		 * 
		 */
		return null;
	}
	
	
	
	
	
	public void removeUsedSupplies() {
		switch (p.getItemInHand().getType()) {
		case WRITTEN_BOOK:
		case BOOK_AND_QUILL:
			int suppliesUsed = 0;
			for (ItemStack is : inkMap.values()) {
				if (suppliesUsed >= copies) break;
				if (copies - suppliesUsed >= is.getAmount()) {
					suppliesUsed += is.getAmount();
					p.getInventory().remove(is);
				} else p.getInventory().remove(new ItemStack(Material.INK_SACK, copies - suppliesUsed, (short) 0));
			}
			
			
			int paperToConsume = maxUncraftedBook * 3;
			for (ItemStack is : paperMap.values()) {
				if (paperToConsume <= 0) break;
				if (paperToConsume >= is.getAmount()) {
					paperToConsume -= is.getAmount();
					consumedSupplies.add(is);
					p.getInventory().remove(is);
				} else {
					ItemStack is1 = is.clone();
					is1.setAmount(paperToConsume);
					consumedSupplies.add(is1);
					p.getInventory().remove(is1);
				}
			}
			
			
			suppliesUsed = 0;
			for (ItemStack is : leatherMap.values()) {
				if (copies - suppliesUsed >= is.getAmount()) {
					suppliesUsed += is.getAmount();
					consumedSupplies.add(is);
					p.getInventory().remove(is);
				} else {
					ItemStack is1 = is.clone();
					is1.setAmount(paperToConsume);
					consumedSupplies.add(is1);
					p.getInventory().remove(is1);
				}
			}
			
			
			suppliesUsed = 0;
			for (ItemStack is : bookMap.values()) {
				if (copies - suppliesUsed >= is.getAmount()) {
					suppliesUsed += is.getAmount();
					consumedSupplies.add(is);
					p.getInventory().remove(is);
				} else {
					ItemStack is1 = is.clone();
					is1.setAmount(paperToConsume);
					consumedSupplies.add(is1);
					p.getInventory().remove(is1);
				}
			}
			break;
		case MAP:
			int requiredPapers = copies * 9;
			for (ItemStack is : paperMap.values()) {
				if (requiredPapers <= 0) break;
				if (requiredPapers >= is.getAmount()) {
					requiredPapers -= is.getAmount();
					consumedSupplies.add(is);
					p.getInventory().remove(is);
				} else {
					ItemStack is1 = is.clone();
					is1.setAmount(requiredPapers);
					consumedSupplies.add(is1);
					p.getInventory().remove(is1);
				}
			}
			break;
		default:
			break;
		}
		
	}
	
	
	
	
	
	//public void refundSupplies
	
	
	
	
	
	public int getMaximumCopiables(Player p) {
		Inventory inv = p.getInventory();
		switch (p.getItemInHand().getType()) {
		case WRITTEN_BOOK:
		case BOOK_AND_QUILL:
			
			inkMap = inv.all(Material.INK_SACK);
			int ink = 0;
			for (Entry<Integer, ? extends ItemStack> e : inkMap.entrySet()) {
				//All dyes share one itemID. We're only interested in actual ink sacks.
				if (e.getValue().getData().getData() != (byte) 0)
					inkMap.remove(e.getKey());
				else ink += e.getValue().getAmount();
			}
			
			leatherMap = inv.all(Material.LEATHER);
			int leather = totalAmount(leatherMap);
			paperMap = inv.all(Material.PAPER);
			int paper = totalAmount(paperMap);
			bookMap = inv.all(Material.BOOK);
			maxUncraftedBook = (leather < (paper / 3) ? leather : paper);
			int total = totalAmount(bookMap) + maxUncraftedBook;
			return ink < total ? ink : total;
		case MAP:
			return totalAmount(paperMap) / 9;
		default: 
			return 0;
		}
	}
	
	
	
	
	
	public int totalAmount(HashMap<Integer, ? extends ItemStack> m) {
		int quantity = 0;
		for (ItemStack is : m.values()) {
			quantity += is.getAmount();
		}
		return quantity;
	}
}
