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
	int ink;
	int book;
	int leather;
	int paper;
	//HashMap<Integer, ? extends ItemStack> blankbaqMap;TODO
	HashMap<Integer, ? extends ItemStack> spaceMap;

	public boolean copy(Player p, int copies) {
		
		switch (p.getItemInHand().getType()) {
		case WRITTEN_BOOK:
			if (!p.hasPermission("booksuite.copy.self"))
				return false;
			break;
		case BOOK_AND_QUILL:
			if (!p.hasPermission("booksuite.copy.unsigned"))
				return false;
			break;
		case MAP:
			if (!p.hasPermission("booksuite.copy.map"))
				return false;
			break;
		default: return false;
		
		
		
		}
		
		
		if (!p.hasPermission("booksuite.copy.free")) {
			int maxPossible = getMaximumCopiables(p);
			if (copies > maxPossible) {
				p.sendMessage(ChatColor.DARK_RED + "You only have the supplies to make " + maxPossible + " copies.");
				copies = maxPossible;
			}
			
		}
		
		/*
		 * Before I forget:
		 * 
		 * 
		 * DO FOR NEATNESS
		 * TODO check remove stack of 64 auto-removing next
		 * --won't work.
		 * 
		 */
		return true;
	}

	public int getMaximumCopiables(Player p) {//TODO
		Inventory inv = p.getInventory();
		switch (p.getItemInHand().getType()) {
		case WRITTEN_BOOK:
		case BOOK_AND_QUILL:
			
			inkMap = inv.all(Material.INK_SACK);
			int ink = 0;
			for (Entry<Integer, ? extends ItemStack> e : inkMap.entrySet()) {
				if (e.getValue().getData().getData() != (byte) 0)
					inkMap.remove(e.getKey());
				else ink += e.getValue().getAmount();
			}
			
			leatherMap = inv.all(Material.LEATHER);
			leather = totalAmount(leatherMap);
			paperMap = inv.all(Material.PAPER);
			paper = totalAmount(paperMap);
			bookMap = inv.all(Material.BOOK);
			int total = totalAmount(bookMap) + (leather < (paper / 3) ? leather : paper);
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
