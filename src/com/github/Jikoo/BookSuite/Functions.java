/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn - ideas and implementation
 *     Ted Meyer - IO assistance and BML (Book Markup Language)
 ******************************************************************************/
package com.github.Jikoo.BookSuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Functions {
	private static Functions instance = null;
	// TODO "encryption" function ADDED A SKIEN ENCRYPTION to .misc
	/*
	 * Honestly what I planned to do for encryption was make the book one page -
	 * a hash of a random UUID or something (SECTION_SIGNk<hash>), save the book as that
	 * if untaken, then allow people to re-import it with the correct hash
	 * (savename). As long as the ingame book editor doesn't improve, I foresee
	 * no issues.
	 */
	private final int[] ACCEPTABLE = { 53, 67, 108, 109, 114, 128, 134, 135,
			136, 156 };

	
	/**
	 * master method for checking if the player can obtain the books
	 * 
	 * @param p
	 *            the player attempting to obtain the book
	 * @return whether the player can obtain the book
	 */
	public boolean canObtainBook(Player p) {
		Inventory inv = p.getInventory();

		if (p.hasPermission("booksuite.book.free")
				|| p.getGameMode().equals(GameMode.CREATIVE)) {
			if (!hasRoom(p)) {
				p.sendMessage(ChatColor.DARK_RED + "Inventory full!");
				return false;
			}
			return true;
		}
		String supplies = checkBookSupplies(inv);
		if (supplies.equals("crafted")) {
			inv.removeItem(new ItemStack(Material.INK_SACK, 1));
			inv.removeItem(new ItemStack(Material.BOOK, 1));
			if (!hasRoom(p)) {
				p.sendMessage(ChatColor.DARK_RED + "Inventory full!");
				inv.addItem(new ItemStack(Material.INK_SACK, 1));
				inv.addItem(new ItemStack(Material.BOOK, 1));
				return false;
			}
			return true;
		}
		if (supplies.equals("uncrafted")) {
			inv.removeItem(new ItemStack(Material.INK_SACK, 1));
			inv.removeItem(new ItemStack(Material.PAPER, 3));
			inv.removeItem(new ItemStack(Material.LEATHER, 1));
			if (!hasRoom(p)) {
				p.sendMessage(ChatColor.DARK_RED + "Inventory full!");
				inv.addItem(new ItemStack(Material.INK_SACK, 1));
				inv.removeItem(new ItemStack(Material.PAPER, 3));
				inv.removeItem(new ItemStack(Material.LEATHER, 1));
				return false;
			}
			return true;
		}
		p.sendMessage(ChatColor.DARK_RED + "To create a book, you need "
				+ supplies + ".");
		return false;
	}

	/**
	 * 
	 * @param p the player
	 * @return whether p (the player) has enough room to store a book
	 */
	public boolean hasRoom(Player p) {
		return p.getInventory().firstEmpty() != -1 || hasStackingRoom(p);
	}

	/**
	 * HELPER FUNCTION for hasRoom
	 * 
	 * @param p the player
	 * @return whether p (the player) has enough room to store a book
	 */
	private boolean hasStackingRoom(Player p) {
		if (!p.hasPermission("booksuite.copy.stack"))
			return false;

		HashMap<Integer, ? extends ItemStack> allBooks = p.getInventory().all(
				p.getItemInHand().getType());
		if (allBooks.size() == 1) {
			return p.getItemInHand().getAmount() < 64;
		} else {
			for (Entry<Integer, ? extends ItemStack> e : allBooks.entrySet()) {
				if (e.getValue().getItemMeta()
						.equals(p.getItemInHand().getItemMeta())) {
					if (e.getValue().getAmount() < 64) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * checks if the player has the supplies needed
	 * 
	 * @param inv
	 *            the inventory of the player
	 * @return whether the player has the supplies needed to copy the book
	 */
	public String checkBookSupplies(Inventory inv) {
		if (inv.contains(Material.BOOK) && inv.contains(Material.INK_SACK)) {
			return "crafted";
		} else if (inv.contains(Material.INK_SACK)) {
			if (inv.contains(new ItemStack(Material.PAPER, 3))
					&& inv.contains(Material.LEATHER)) {
				return "uncrafted";
			}
			return "a book";
		} else if (inv.contains(Material.BOOK)) {
			return "an ink sack";
		}
		return "a book and an ink sack";
	}

	/**
	 * @param a
	 *            the author of the book to be copied
	 * @return true if the player has permission to copy
	 */
	public boolean checkCopyPermission(Player p, String a) {
		if (p.hasPermission("booksuite.copy.other"))
			return true;
		if (p.hasPermission("booksuite.copy.self") && a.equals(p.getName()))
			return true;
		else if (p.hasPermission("booksuite.copy.self"))
			p.sendMessage(ChatColor.DARK_RED
					+ "You do not have permission to copy others' books.");
		else
			p.sendMessage(ChatColor.DARK_RED
					+ "You do not have permission to copy books.");
		return false;
	}

	/**
	 * @param a
	 *            the author of the book to be copied
	 * @return true if the player has permission to copy
	 */
	public boolean checkCommandCopyPermission(Player p, String a) {
		if (p.hasPermission("booksuite.command.copy.other"))
			return true;
		if (p.hasPermission("booksuite.command.copy")
				&& (a.equals(null) || a.equals(p.getName())))
			return true;
		else if (p.hasPermission("booksuite.command.copy")) {
			p.sendMessage(ChatColor.DARK_RED
					+ "You do not have permission to copy others' books.");
			return false;
		} else
			return false;
	}

	/**
	 * 
	 * copies the book that the player is currently holding
	 * 
	 * @param p the player
	 */
	@SuppressWarnings("deprecation")
	public void copy(Player p) {
		if (p.hasPermission("booksuite.copy.stack")
				&& !p.getItemInHand().getType().equals(Material.MAP)) {
			if (p.getItemInHand().getAmount() == 64) {
				HashMap<Integer, ? extends ItemStack> allBooks = p
						.getInventory().all(p.getItemInHand().getType());
				if (allBooks.size() == 1) {
					newDuplicate(p);
				} else {
					boolean copiedSuccessfully = false;
					for (Entry<Integer, ? extends ItemStack> e : allBooks
							.entrySet()) {
						if (e.getValue().getItemMeta()
								.equals(p.getItemInHand().getItemMeta())) {
							if (e.getValue().getAmount() < 64) {
								ItemStack book = e.getValue();
								book.setAmount(e.getValue().getAmount() + 1);
								p.getInventory().setItem(e.getKey(), book);
								copiedSuccessfully = true;
								break;
							}
						}
					}
					if (!copiedSuccessfully) {
						newDuplicate(p);
					}
				}
			} else {
				p.getItemInHand().setAmount(p.getItemInHand().getAmount() + 1);
			}
		} else {
			newDuplicate(p);
		}
		p.updateInventory();
	}

	/**
	 * 
	 * @param p the player in whose inventory the duplication will be done
	 */
	public void newDuplicate(Player p) {
		ItemStack duplicate = p.getItemInHand().clone();
		duplicate.setAmount(1);
		p.getInventory().addItem(duplicate);
	}

	/**
	 * 
	 * @param p the player who triggers the unsign event
	 * @return  whether the unsigning was successful
	 */
	public boolean unsign(Player p) {
		ItemStack unsign = p.getItemInHand();
		if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)
				&& !p.getItemInHand().getType().equals(Material.BOOK_AND_QUILL)) {
			return false;
		}
		BookMeta unsignMeta = (BookMeta) unsign.getItemMeta();
		unsignMeta.setAuthor(null);
		unsignMeta.setTitle(null);
		unsign.setItemMeta(unsignMeta);
		unsign.setType(Material.BOOK_AND_QUILL);
		return true;
	}

	/**
	 * 
	 * @param p the player who triggers the event
	 * @param newAuthor the name of the author to set as the book's author
	 * @return whether the change of author was successful
	 */
	public boolean setAuthor(Player p, String newAuthor) {
		ItemStack book = p.getItemInHand();
		if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK))
			return false;
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setAuthor(newAuthor);
		book.setItemMeta(bookMeta);
		return true;
	}

	/**
	 * 
	 * @param p the player who triggers the event
	 * @param newTitle the new title of the book to be set
	 * @return whether the setting of the title was completed successfully
	 */
	public boolean setTitle(Player p, String newTitle) {
		if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK))
			return false;
		ItemStack book = p.getItemInHand();
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		bookMeta.setTitle(newTitle);
		book.setItemMeta(bookMeta);
		return true;
	}

	/**
	 * 
	 * @param p the player wo triggers the event
	 * @param pageNumber the place the page is to be inserted
	 * @param text the text that goes on the page
	 * @return the whether the user was successfull in their attempt at inserting a page
	 */
	public boolean insertPageAt(Player p, String pageNumber, String text) {
		if (!p.getItemInHand().getType().equals(Material.BOOK_AND_QUILL)) {
			p.sendMessage(ChatColor.DARK_RED
					+ "You must be holding a book and quill to use this command!");
			return false;
		}
		ItemStack book = p.getItemInHand();
		BookMeta bm = (BookMeta) book.getItemMeta();
		List<String> pages = new ArrayList<String>();
		try {
			int page = Integer.parseInt(pageNumber);
			for (int i = 1; i <= bm.getPageCount(); i++) {
				if (i == page)
					pages.add(text);
				pages.add(bm.getPage(i));
			}
			bm.setPages(pages);
		} catch (NumberFormatException e) {
			p.sendMessage(ChatColor.DARK_RED
					+ "Correct usage is \"/book addpage <page number> [optional page text]\"");
			return false;
		} catch (IndexOutOfBoundsException e1) {
			p.sendMessage(ChatColor.DARK_RED
					+ "Please enter a number between 1 and "
					+ (bm.getPageCount() - 1) + ".");
			return false;
		} catch (Exception e2) {
			System.err.println("[BookSuite] Functions.insertPageAt: " + e2);
			e2.printStackTrace();
			System.err.println("[BookSuite] End error report.");
		}
		book.setItemMeta(bm);
		return true;
	}

	/**
	 * 
	 * @param p the player who triggers the event
	 * @param pageNumber the page to be deleted
	 * @return whether the player was successful in deleting the page
	 */
	public boolean deletePageAt(Player p, String pageNumber) {
		if (!p.getItemInHand().getType().equals(Material.BOOK_AND_QUILL)) {
			p.sendMessage(ChatColor.DARK_RED
					+ "You must be holding a book and quill to use this command!");
			return false;
		}
		ItemStack book = p.getItemInHand();
		BookMeta bm = (BookMeta) book.getItemMeta();
		List<String> pages = new ArrayList<String>();
		try {
			int page = Integer.parseInt(pageNumber);
			for (int i = 1; i <= bm.getPageCount(); i++) {
				if (i != page)
					pages.add(bm.getPage(i));
			}
			bm.setPages(pages);
		} catch (NumberFormatException e) {
			p.sendMessage(ChatColor.DARK_RED
					+ "Correct usage is \"/book delpage <page number>\"");
			return false;
		} catch (IndexOutOfBoundsException e1) {
			p.sendMessage(ChatColor.DARK_RED
					+ "Please enter a number between 1 and "
					+ (bm.getPageCount() - 1) + ".");
			return false;
		} catch (Exception e2) {
			System.err.println("[BookSuite] Functions.insertPageAt: " + e2);
			e2.printStackTrace();
			System.err.println("[BookSuite] End error report.");
		}
		book.setItemMeta(bm);
		return true;
	}

	/**
	 * 
	 * @param p the player who triggered the event and whom is to be tested for this property
	 * @return whether p (the player) can obtain a map
	 */
	public boolean canObtainMap(Player p) {
		if (p.hasPermission("booksuite.copy.map")) {
			Inventory inv = p.getInventory();
			if (p.hasPermission("booksuite.book.free")
					|| p.getGameMode().equals(GameMode.CREATIVE)) {
				if (inv.firstEmpty() == -1
						&& p.getItemInHand().getAmount() == 64) {
					p.sendMessage(ChatColor.DARK_RED + "Inventory full!");
					return false;
				} else
					return true;
			} else if (inv.contains(new ItemStack(Material.PAPER, 9))) {
				inv.remove(new ItemStack(Material.PAPER, 9));
				if (inv.firstEmpty() == -1) {
					inv.addItem(new ItemStack(Material.PAPER, 9));
					p.sendMessage(ChatColor.DARK_RED + "Inventory full!");
					return false;
				} else
					return true;
			} else {
				p.sendMessage(ChatColor.DARK_RED
						+ "You need 9 paper to copy a map.");
				return false;
			}
		} else {
			p.sendMessage(ChatColor.DARK_RED
					+ "You do not have permission to copy maps.");
			return false;
		}
	}

	/**
	 * tests if a given block is an inverted stair block
	 * 
	 * @param b
	 *            the block to be tested
	 * @return whether the block is an inverted stair
	 */
	public boolean isInvertedStairs(Block b) {
		for (int i : ACCEPTABLE)
			if (i == b.getTypeId())
				return b.getData() > 3;
		return false;
	}

	/**
	 * 
	 * @param is the stack of stairs to be tested
	 * @return whether the item is an acceptable type of stair
	 */
	public boolean isCorrectStairType(ItemStack is) {
		for (int i : ACCEPTABLE)
			if (i == is.getTypeId())
				return true;
		return false;
	}

	/**
	 * 
	 * @param p the player who triggered the event
	 * @return the proper orientation byte for the stair
	 */
	public byte getCorrectStairOrientation(Player p) {
		byte playerFace = (byte) Math.round(p.getLocation().getYaw() / 90);
		if (playerFace == 0 || playerFace == -4 || playerFace == 4)
			return 6;// open north
		else if (playerFace == 1 || playerFace == -3)
			return 5;// open east
		else if (playerFace == 2 || playerFace == -2)
			return 7;// open south
		else
			return 4;// open west
	}
	

	/**
	 * 
	 * @param blockToCheck the block to check
	 * @return whether this block is a stair or crafting table part of a printing press
	 */
	public boolean isPrintingPress(Block blockToCheck) {
		if (!BookSuite.getInstance().getConfig()
				.getBoolean("enable-printing-presses")) {
			return false;
		}
		if (blockToCheck.getType().equals(Material.WORKBENCH)) {
			if (isInvertedStairs(blockToCheck.getRelative(BlockFace.UP))) {
				return true;
			}
		}
		if (isInvertedStairs(blockToCheck)) {
			if (blockToCheck.getRelative(BlockFace.DOWN).getType()
					.equals(Material.WORKBENCH)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param clicked the clicked block
	 * @param clickedFace the face of the block that was clicked
	 * @param itemInHand the item in the hand of the player
	 * @param p the player who clicked
	 * @return whether the player is allowed and has met the criteria for creating a printing press
	 */
	public boolean canMakePress(Block clicked, BlockFace clickedFace,
			ItemStack itemInHand, Player p) {
		if (!clickedFace.equals(BlockFace.UP)) {
			return false;
		}
		if (!clicked.getType().equals(Material.WORKBENCH)) {
			return false;
		}
		if (!p.hasPermission("booksuite.copy.createpress")) {
			return false;
		}
		if (!isCorrectStairType(itemInHand)) {
			return false;
		}
		if (clicked.getRelative(BlockFace.UP).getType() != Material.AIR) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param clicked the block that was clicked
	 * @param itemInHand the item the user was holding when the event was triggered
	 * @return whether the user who triggered the event is able to erase this book (note: permissions are factored in later)
	 */
	public boolean canErase(Block clicked, ItemStack itemInHand) {
		if (!itemInHand.getType().equals(Material.WRITTEN_BOOK)) {
			return false;
		}
		if (!clicked.getType().equals(Material.CAULDRON)) {
			return false;
		}
		if (!itemInHand.hasItemMeta()) {
			return false;
		}
		if (itemInHand.getItemMeta() == null) {
			return false;
		}
		if (!BookSuite.getInstance().getConfig().getBoolean("enable-erasers")) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param clicked the block that was clicked
	 * @return whether the block is a sign or chest that is part of a mailbox
	 */
	public boolean isMailBox(Block clicked) {
		Sign sign = null;
		if (clicked.getType().equals(Material.SIGN)) {
			sign = (Sign) clicked;
		} else if (clicked.getType().equals(Material.CHEST)) {
			Block up = clicked.getRelative(BlockFace.UP);
			if (up instanceof Sign) {
				sign = (Sign) up;
			} else {
				return false;
			}
		}
		// rudimentary example
		return sign.getLine(0).equals(
				ChatColor.DARK_RED + "No sign line can contain this string.");
	}

	/**
	 * 
	 * TODO: MAKE THIS WORK
	 * 
	 * @param clicked the block that was clicked
	 * @param clicker the player who clicked the block
	 * @return whether block that the player clicked is part of a library
	 */
	public boolean isLibrary(Block clicked, Player clicker) {
		return false;
	}

	/**
	 * 
	 * SINGLETON
	 * 
	 * @return an instance of the functions file for use in other classes
	 */
	public static Functions getInstance() {
		if (instance == null)
			instance = new Functions();
		return instance;
	}

}
