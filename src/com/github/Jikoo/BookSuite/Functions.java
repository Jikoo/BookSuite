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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Functions {
	private static Functions instance = null;

	/**
	 * master method for checking if the player can obtain the books
	 * 
	 * @param p the player attempting to obtain the book
	 * 
	 * @return whether the player can obtain the book
	 */
	public boolean canObtainBook(CommandSender s) {
		if (!(s instanceof Player)) {
			return true;
		}

		Player p = (Player) s;
		Inventory inv = p.getInventory();

		if (p.hasPermission("booksuite.book.free") || p.getGameMode().equals(GameMode.CREATIVE)) {
			if (inv.firstEmpty() == -1) {
				p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_SPACE"));
				return false;
			}
			return true;
		}
		String supplies = checkBookSupplies(inv);
		if (supplies == null) {
			inv.removeItem(new ItemStack(Material.INK_SACK, 1));
			inv.removeItem(new ItemStack(Material.BOOK, 1));
			if (inv.firstEmpty() == -1) {
				p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_SPACE"));
				inv.addItem(new ItemStack(Material.INK_SACK, 1));
				inv.addItem(new ItemStack(Material.BOOK, 1));
				return false;
			}
			return true;
		}
		p.sendMessage(supplies);
		return false;
	}

	/**
	 * checks if the player has the supplies needed
	 * 
	 * @param inv the inventory of the player
	 * 
	 * @return whether the player has the supplies needed to copy the book
	 */
	public String checkBookSupplies(Inventory inv) {
		if (inv.contains(Material.BOOK) && inv.contains(Material.INK_SACK)) {
			return null;
		} else if (inv.contains(Material.INK_SACK)) {
			return BookSuite.getInstance().msgs.get("FAILURE_COPY_BOOK");
		} else if (inv.contains(Material.BOOK)) {
			return BookSuite.getInstance().msgs.get("FAILURE_COPY_INK");
		}
		return BookSuite.getInstance().msgs.get("FAILURE_COPY_BOTH");
	}

	/**
	 * @param a the author of the book to be copied
	 * 
	 * @return true if the player has permission to copy
	 */
	public boolean checkCopyPermission(Player p, String a) {
		if (p.hasPermission("booksuite.copy.other"))
			return true;
		if (p.hasPermission("booksuite.copy.self") && isAuthor(p, a))
			return true;
		else if (p.hasPermission("booksuite.copy.self"))
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_PERMISSION_COPY_OTHER"));
		else
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_PERMISSION_COPY"));
		return false;
	}

	/**
	 * @param a the author of the book to be copied
	 * 
	 * @return true if the player has permission to copy
	 */
	public boolean checkCommandCopyPermission(Player p, String a) {
		if (p.hasPermission("booksuite.command.copy.other"))
			return true;
		if (p.hasPermission("booksuite.command.copy") && (p.getName().equals(a) || p.getDisplayName().equals(a)))
			return true;
		if (p.hasPermission("booksuite.command.copy")) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_PERMISSION_COPY_OTHER"));
			return false;
		}
		p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_PERMISSION_COPY"));
		return false;
	}

	/**
	 * Copies the book that the player is currently holding
	 * 
	 * @param p the player
	 */
	@SuppressWarnings("deprecation")
	public void copy(Player p, int quantity) {
		ItemStack duplicate = p.getItemInHand().clone();
		duplicate.setAmount(duplicate.getMaxStackSize());
		while (quantity > 0) {
			if (quantity > duplicate.getMaxStackSize()) {
				p.getInventory().addItem(duplicate);
				quantity -= duplicate.getMaxStackSize();
			} else {
				duplicate.setAmount(quantity);
				p.getInventory().addItem(duplicate);
				quantity = 0;
			}
		}
		p.updateInventory();
	}

	/**
	 * Unsigns a book in the specified player's hand.
	 * 
	 * @param p the player who triggers the unsign event
	 * 
	 * @return whether the unsigning was successful
	 */
	public boolean unsign(Player p) {
		ItemStack unsign = p.getItemInHand();
		if (unsign.getType() != Material.WRITTEN_BOOK
				&& unsign.getType() != Material.BOOK_AND_QUILL) {
			return false;
		}
		BookMeta unsignMeta = (BookMeta) unsign.getItemMeta();
		unsignMeta.setTitle(null);
		unsign.setItemMeta(unsignMeta);
		unsign.setType(Material.BOOK_AND_QUILL);
		return true;
	}

	/**
	 * 
	 * @param p the player who triggers the event
	 * @param pageNumber the place the page is to be inserted
	 * @param text the text that goes on the page
	 * 
	 * @return whether the user was successful in their attempt at inserting a page
	 */
	public boolean insertPageAt(Player p, String pageNumber, String text) {
		if (!p.getItemInHand().getType().equals(Material.BOOK_AND_QUILL)) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_EDIT_NOBAQ"));
			return false;
		}
		ItemStack book = p.getItemInHand();
		BookMeta bm = (BookMeta) book.getItemMeta();
		List<String> pages = new ArrayList<String>(bm.getPages());
		try {
			pages.add(Integer.parseInt(pageNumber), text);
			bm.setPages(pages);
		} catch (NumberFormatException e) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_USAGE") + BookSuite.getInstance().msgs.get("USAGE_EDIT_ADDPAGE"));
			return false;
		} catch (IndexOutOfBoundsException e) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_EDIT_INVALIDNUMBER"));
			return false;
		} catch (Exception e) {
			BSLogger.err(e);
		}
		book.setItemMeta(bm);
		return true;
	}

	/**
	 * 
	 * @param p the player who triggers the event
	 * @param pageNumber the page to be deleted
	 * 
	 * @return whether the player was successful in deleting the page
	 */
	public boolean deletePageAt(Player p, String pageNumber) {
		if (!p.getItemInHand().getType().equals(Material.BOOK_AND_QUILL)) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_EDIT_NOBAQ"));
			return false;
		}
		ItemStack book = p.getItemInHand();
		BookMeta bm = (BookMeta) book.getItemMeta();
		List<String> pages = new ArrayList<String>(bm.getPages());
		try {
			pages.remove(Integer.parseInt(pageNumber) + 1);
			bm.setPages(pages);
		} catch (NumberFormatException e) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_USAGE") + BookSuite.getInstance().msgs.get("USAGE_EDIT_DELPAGE"));
			return false;
		} catch (IndexOutOfBoundsException e) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_EDIT_INVALIDNUMBER"));
			return false;
		} catch (Exception e) {
			BSLogger.err(e);
		}
		book.setItemMeta(bm);
		return true;
	}

	/**
	 * 
	 * @param p the player who triggered the event
	 * 
	 * @return whether p (the player) can obtain a map
	 */
	public boolean canObtainMap(Player p) {
		Inventory inv = p.getInventory();
		if (p.hasPermission("booksuite.book.free") || p.getGameMode().equals(GameMode.CREATIVE)) {
			if (inv.firstEmpty() == -1 && p.getItemInHand().getAmount() == 64) {
				p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_SPACE"));
				return false;
			} else {
				return true;
			}
		}
		for (int i = 0; i < 9; i++) {
			if (inv.contains(Material.PAPER)) {
				inv.removeItem(new ItemStack(Material.PAPER));
			} else {
				inv.addItem(new ItemStack(Material.PAPER, i));
				p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_COPY_MAP"));
				return false;
			}
		}
		if (inv.firstEmpty() == -1) {
			inv.addItem(new ItemStack(Material.PAPER, 9));
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_SPACE"));
			return false;
		} else {
			return true;
		}
	}

	/**
	 * tests if a given block is an inverted stair block
	 * 
	 * @param b the block to be tested
	 * 
	 * @return whether the block is an inverted stair
	 */
	@SuppressWarnings("deprecation")
	public boolean isInvertedStairs(Block b) {
		if (b.getType().name().contains("STAIRS")) {
			return b.getData() > 3;
		}
		return false;
	}

	/**
	 * 
	 * @param is the stack of stairs to be tested
	 * 
	 * @return whether the item is an acceptable type of stair
	 */
	public boolean isCorrectStairType(ItemStack is) {
		return is.getType().name().contains("STAIRS");
	}

	/**
	 * 
	 * @param p the player who triggered the event
	 * 
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
	 * 
	 * @return whether this block is part of a printing press
	 */
	public boolean isPrintingPress(Block blockToCheck) {
		if (!BookSuite.getInstance().getConfig().getBoolean("enable-printing-presses")) {
			return false;
		}
		if (blockToCheck.getType() == Material.WORKBENCH) {
			if (isInvertedStairs(blockToCheck.getRelative(BlockFace.UP))) {
				return true;
			}
		}
		if (isInvertedStairs(blockToCheck)) {
			if (blockToCheck.getRelative(BlockFace.DOWN).getType() == Material.WORKBENCH) {
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
	 * 
	 * @return whether the player meets the criteria for creating a printing press
	 */
	public boolean canMakePress(Block clicked, BlockFace clickedFace, ItemStack itemInHand, Player p) {
		if (clickedFace != BlockFace.UP) {
			return false;
		}
		if (clicked.getType() != Material.WORKBENCH) {
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
	 * 
	 * @return whether the user who triggered the event is able to erase this
	 *         book (note: permissions are factored in later)
	 */
	public boolean canErase(Block clicked, ItemStack itemInHand) {
		if (itemInHand.getType() != Material.WRITTEN_BOOK
				&& itemInHand.getType() != Material.BOOK_AND_QUILL) {
			return false;
		}
		if (clicked.getType() != Material.CAULDRON) {
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
	 * 
	 * @return whether the block is a sign or chest that is part of a mailbox
	 */
	public boolean isMailBox(Block clicked) {
		if (!BookSuite.getInstance().getConfig().getBoolean("enable-mail")) {
			return false;
		}
		Sign sign = null;
		if (clicked.getState() instanceof Sign) {
			sign = (Sign) clicked.getState();
		} else if (clicked.getType() == Material.CHEST) {
			BlockState up = clicked.getRelative(BlockFace.UP).getState();
			if (up instanceof Sign) {
				sign = (Sign) up;
			} else {
				return false;
			}
		}
		return sign != null && "mail".equalsIgnoreCase(sign.getLine(0));
	}

	/**
	 * Parses Book Markup Language (BML)
	 * 
	 * @param text the <code>String</code> to parse
	 * 
	 * @return the <code>String</code> after parsing
	 */
	public String parseBML(String text) {
		if (text == null) {
			return null;
		}
		text = text.replaceAll("(<|\\[)[Ii]([Tt][Aa][Ll][Ii][Cc][Ss]?)?(>|\\])", ChatColor.ITALIC.toString());
		text = text.replaceAll("(<|\\[)[Bb]([Oo][Ll][Dd])?(>|\\])", ChatColor.BOLD.toString());
		text = text.replaceAll("(<|\\[)[Uu]([Nn][Dd][Ee][Rr][Ll][Ii][Nn][Ee][Dd]?)?(>|\\])", ChatColor.UNDERLINE.toString());
		text = text.replaceAll("(<|\\[)([Ss]([Tt][Rr][Ii][Kk][Ee]([Tt][Hh][Rr][Oo][Uu][Gg][Hh])?)?|[Dd][Ee][Ll])(>|\\])", ChatColor.STRIKETHROUGH.toString());
		text = text.replaceAll("(<|\\[)([Mm]([Aa][Gg][Ii][Cc])?|[Oo][Bb][Ff]([Uu][Ss][Cc][Aa][Tt][Ee][Dd]?)?)(>|\\])", ChatColor.MAGIC.toString());

		text = text.replaceAll("(<|\\[)[Cc][Oo][Ll][Oo][Rr]=", "<");
		text = text.replaceAll("(<|\\[)[Bb][Ll][Aa][Cc][Kk](>|\\])", ChatColor.BLACK.toString());
		text = text.replaceAll("(<|\\[)[Dd][Aa][Rr][Kk]_?[Bb][Ll][Uu][Ee](>|\\])", ChatColor.DARK_BLUE.toString());
		text = text.replaceAll("(<|\\[)[Dd][Aa][Rr][Kk]_?[Gg][Rr][Ee][Ee][Nn](>|\\])", ChatColor.DARK_GREEN.toString());
		text = text.replaceAll("(<|\\[)[Dd][Aa][Rr][Kk]_?[Aa][Qq][Uu][Aa](>|\\])", ChatColor.DARK_AQUA.toString());
		text = text.replaceAll("(<|\\[)[Dd][Aa][Rr][Kk]_?[Rr][Ee][Dd](>|\\])", ChatColor.DARK_RED.toString());
		text = text.replaceAll("(<|\\[)([Dd][Aa][Rr][Kk]_?)?[Pp][Uu][Rr][Pp][Ll][Ee](>|\\])", ChatColor.DARK_PURPLE.toString());
		text = text.replaceAll("(<|\\[)([Dd][Aa][Rr][Kk]_?)?[Gg][Oo][Ll][Dd](>|\\])", ChatColor.GOLD.toString());
		text = text.replaceAll("(<|\\[)[Gg][Rr][AEae][Yy](>|\\])", ChatColor.GRAY.toString());
		text = text.replaceAll("(<|\\[)[Dd][Aa][Rr][Kk]_?[Gg][Rr][AEae][Yy](>|\\])", ChatColor.DARK_GRAY.toString());
		text = text.replaceAll("(<|\\[)([Ii][Nn][Dd][Ii][Gg][Oo]|(([Ll]|[Bb][Rr])[Ii][Gg][Hh][Tt]_?)?[Bb][Ll][Uu][Ee])(>|\\])", ChatColor.BLUE.toString());
		text = text.replaceAll("(<|\\[)(([Ll]|[Bb][Rr])[Ii][Gg][Hh][Tt]_?)?[Gg][Rr][Ee][Ee][Nn](>|\\])", ChatColor.GREEN.toString());
		text = text.replaceAll("(<|\\[)[Aa][Qq][Uu][Aa](>|\\])", ChatColor.AQUA.toString());
		text = text.replaceAll("(<|\\[)(([Ll]|[Bb][Rr])[Ii][Gg][Hh][Tt]_?)?[Rr][Ee][Dd](>|\\])", ChatColor.RED.toString());
		text = text.replaceAll("(<|\\[)((([Ll]|[Bb][Rr])[Ii][Gg][Hh][Tt]_?)?[Pp][Uu][Rr][Pp][Ll][Ee]|[Pp][Ii][Nn][Kk]|[Mm][Aa][Gg][Ee][Nn][Tt][Aa])(>|\\])", ChatColor.LIGHT_PURPLE.toString());
		text = text.replaceAll("(<|\\[)[Yy][Ee][Ll][Ll][Oo][Ww](>|\\])", ChatColor.YELLOW.toString());
		text = text.replaceAll("(<|\\[)[Ww][Hh][Ii][Tt][Ee](>|\\])", ChatColor.WHITE.toString());

		text = ChatColor.translateAlternateColorCodes('&', text);

		text = text.replaceAll("(<|\\[)/([Ii]([Tt][Aa][Ll][Ii][Cc][Ss]?)?|[Bb]([Oo][Ll][Dd])?|[Uu]([Nn][Dd][Ee][Rr][Ll][Ii][Nn][Ee][Dd]?)?"
				+ "|[Ss]([Tt][Rr][Ii][Kk][Ee]([Tt][Hh][Rr][Oo][Uu][Gg][Hh])?)?|[Dd][Ee][Ll]|[Ff][Oo][Rr][Mm][Aa][Tt]"
				+ "|[Mm]([Aa][Gg][Ii][Cc])?|[Oo][Bb][Ff]([Uu][Ss][Cc][Aa][Tt][Ee][Dd]?)?)(>|\\])", ChatColor.RESET.toString());
		text = text.replaceAll("(<|\\[)/[Cc][Oo][Ll][Oo][Rr](>|\\])", ChatColor.WHITE.toString());
		text = text.replaceAll("(<|\\[)[Hh][Rr](>|\\])", "\n-------------------\n");
		text = text.replaceAll("(<|\\[)([Nn]|[Bb][Rr])(>|\\])", "\n");
		text = text.replaceAll("(" + ChatColor.RESET + ")+", ChatColor.RESET.toString());
		return text;
	}

	/**
	 * Translate &code colors into ChatColors.
	 * 
	 * @param s the String to translate codes in
	 * 
	 * @return the translated String
	 */
	public String addColor(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	/**
	 * Lists book files in directory
	 * 
	 * @param directory the directory
	 * @param p the <code>Player</code> to obtain file list
	 */
	public void listBookFilesIn(String directory, Player p) {
		final File file = new File(directory);
		if (!file.exists()) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_LIST_NOBOOKS"));
			return;
		}
		File[] publicBooks = file.listFiles();
		if (publicBooks == null || publicBooks.length == 0) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_LIST_NOBOOKS"));
			return;
		}
		String bookList = new String(); // TODO fix
		if (publicBooks != null && publicBooks.length != 0) {
			p.sendMessage(BookSuite.getInstance().msgs.get("SUCCESS_LIST"));
			for (File bookFile : publicBooks) {
				bookList += bookFile.getName().replace(".book", "") + ", ";
				// Maximum allowed characters in a server-to-client chat message is 32767.
				// In the event that people actively try to mess things up or
				// there are an obscene amount of books, we'll cut it off.
				// 
				// Maximum book name from in-game save is 92 characters.
				// Server admins will just have to not be stupid.
				if (bookList.length() > 32500) {
					p.sendMessage(ChatColor.DARK_GREEN
							+ bookList.substring(0, bookList.length() - 2));
					bookList = new String();
				}
			}
			p.sendMessage(ChatColor.DARK_GREEN + bookList.substring(0, bookList.length() - 2));
		}
	}

	public boolean isAuthor(Player p, String author) {
		return author == null || p.getName().equals(author) || BookSuite.getInstance().getConfig().getBoolean("enable-aliases") && author.contains(p.getDisplayName());
	}

	protected void disable() {
		instance = null;
	}

	/**
	 * SINGLETON
	 * 
	 * @return an instance of the functions file for use in other classes
	 */
	protected static Functions getInstance() {
		if (instance == null)
			instance = new Functions();
		return instance;
	}
}
