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
package com.github.Jikoo.BookSuite.module.copy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Copier {
	Map<Integer, ? extends ItemStack> inkMap;
	Map<Integer, ? extends ItemStack> bookMap;
	Map<Integer, ? extends ItemStack> paperMap;
	int bookTotal;
	int inkTotal;

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
		default:
			return CopyFailureReason.UNCOPIABLE;
		}

		if (!p.hasPermission("booksuite.copy.free")) {
			int maxPossible = getMaximumCopiables(p);
			if (copies > maxPossible) {
				p.sendMessage(ChatColor.DARK_RED
						+ "You only have the supplies to make " + maxPossible
						+ " copies.");
				copies = maxPossible;
			}

			//p.getInventory().getContents().length - p.getInventory().getSize();
			// num starting free slots
			// calc merge freed
			// if merge remainder < copies add 1
			// remove supplies
		}

		// check free space

		/*
		 * Before I forget:
		 * 
		 * 
		 * DO FOR NEATNESS TODO check remove stack of 64 auto-removing next
		 * --won't work.
		 */
		return null;
	}

	public void removeUsedSupplies() {
		switch (p.getItemInHand().getType()) {
		case WRITTEN_BOOK:
		case BOOK_AND_QUILL:
			int suppliesUsed = 0;
			for (ItemStack is : inkMap.values()) {
				if (suppliesUsed >= copies)
					break;
				if (copies - suppliesUsed >= is.getAmount()) {
					suppliesUsed += is.getAmount();
					p.getInventory().remove(is);
				} else
					p.getInventory().remove(
							new ItemStack(Material.INK_SACK, copies
									- suppliesUsed, (short) 0));
			}

			suppliesUsed = 0;
			for (ItemStack is : bookMap.values()) {
				if (copies - suppliesUsed >= is.getAmount()) {
					suppliesUsed += is.getAmount();
					consumedSupplies.add(is);
					p.getInventory().remove(is);
				} else {
					ItemStack is1 = is.clone();
					is1.setAmount(suppliesUsed);
					consumedSupplies.add(is1);
					p.getInventory().remove(is1);
					break;
				}
			}
			break;
		case MAP:
			int requiredPapers = copies * 9;
			for (ItemStack is : paperMap.values()) {
				if (requiredPapers <= 0)
					break;
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

	// public void refundSupplies

	public int calcMergeFreedSpace(HashMap<Integer, ItemStack> stacks) {
		return stacks.size() - (int) Math.ceil(totalAmount(stacks) / 64d) ;
	}

	public Inventory removeAddMerge(Inventory i, ItemStack is, int quantity) {
		Material m = is.getType();

		if (m != Material.INK_SACK) {
			i.remove(m);
		} else {
			for (Entry<Integer, ? extends ItemStack> e : i.all(m).entrySet()) {
				if (e.getValue().getData().getData() == (byte) 0) {
					i.remove(e.getKey());
				}
			}
		}

		while (quantity > 0) {
			int added;
			if (quantity > 64)
				added = 64;
			else
				added = quantity;
			i.addItem(new ItemStack(m, added));
			quantity -= added;
		}

		return i;
	}

	public int getMaximumCopiables(Player p) {
		Inventory inv = p.getInventory();
		switch (p.getItemInHand().getType()) {
		case WRITTEN_BOOK:
		case BOOK_AND_QUILL:

			inkMap = inv.all(Material.INK_SACK);
			int ink = totalInk(inkMap);
			bookMap = inv.all(Material.BOOK);
			int total = totalAmount(bookMap);
			return ink < total ? ink : total;
		case MAP:
			return totalAmount(paperMap) / 9;
		default:
			return 0;
		}
	}

	private int totalInk(Map<Integer, ? extends ItemStack> m) {
		int ink = 0;
		for (Entry<Integer, ? extends ItemStack> e : inkMap.entrySet()) {
			// All dyes share one itemID. We're only interested in actual
			// ink sacks.
			if (e.getValue().getData().getData() != (byte) 0)
				inkMap.remove(e.getKey());
			else
				ink += e.getValue().getAmount();
		}
		return ink;
	}

	private int totalAmount(Map<Integer, ? extends ItemStack> m) {
		int quantity = 0;
		for (ItemStack is : m.values()) {
			quantity += is.getAmount();
		}
		return quantity;
	}
}
