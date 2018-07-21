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
package com.github.jikoo.booksuite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Functions {

	private final BookSuite plugin;

	Functions(BookSuite plugin) {
		this.plugin = plugin;
	}

	/**
	 * Master method for checking if the player can obtain the books.
	 *
	 * @param s the CommandSender attempting to obtain a book
	 * @return whether the player can obtain the book
	 */
	boolean canObtainBook(CommandSender s) {
		if (!(s instanceof Player)) {
			return true;
		}

		Player p = (Player) s;
		Inventory inv = p.getInventory();

		if (p.hasPermission("booksuite.book.free") || p.getGameMode().equals(GameMode.CREATIVE)) {
			if (inv.firstEmpty() == -1) {
				p.sendMessage(plugin.getMessages().get("FAILURE_SPACE"));
				return false;
			}
			return true;
		}
		String supplies = checkBookSupplies(inv);
		if (supplies == null) {
			inv.removeItem(new ItemStack(Material.INK_SAC, 1));
			inv.removeItem(new ItemStack(Material.BOOK, 1));
			if (inv.firstEmpty() == -1) {
				p.sendMessage(plugin.getMessages().get("FAILURE_SPACE"));
				inv.addItem(new ItemStack(Material.INK_SAC, 1));
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
	private String checkBookSupplies(Inventory inv) {
		if (inv.contains(Material.BOOK) && inv.contains(Material.INK_SAC)) {
			return null;
		} else if (inv.contains(Material.INK_SAC)) {
			return plugin.getMessages().get("FAILURE_COPY_BOOK");
		} else if (inv.contains(Material.BOOK)) {
			return plugin.getMessages().get("FAILURE_COPY_INK");
		}
		return plugin.getMessages().get("FAILURE_COPY_BOTH");
	}

	/**
	 * @param a the author of the book to be copied
	 *
	 * @return true if the player has permission to copy
	 */
	boolean checkCopyPermission(Player p, String a) {
		if (p.hasPermission("booksuite.copy.other"))
			return true;
		if (p.hasPermission("booksuite.copy.self") && isAuthor(p, a))
			return true;
		else if (p.hasPermission("booksuite.copy.self"))
			p.sendMessage(plugin.getMessages().get("FAILURE_PERMISSION_COPY_OTHER"));
		else
			p.sendMessage(plugin.getMessages().get("FAILURE_PERMISSION_COPY"));
		return false;
	}

	/**
	 * @param a the author of the book to be copied
	 *
	 * @return true if the player has permission to copy
	 */
	boolean checkCommandCopyPermission(Player p, String a) {
		if (p.hasPermission("booksuite.command.copy.other"))
			return true;
		if (p.hasPermission("booksuite.command.copy") && (p.getName().equals(a) || p.getDisplayName().equals(a)))
			return true;
		if (p.hasPermission("booksuite.command.copy")) {
			p.sendMessage(plugin.getMessages().get("FAILURE_PERMISSION_COPY_OTHER"));
			return false;
		}
		p.sendMessage(plugin.getMessages().get("FAILURE_PERMISSION_COPY"));
		return false;
	}

	/**
	 * Copies the book that the player is currently holding
	 *
	 * @param p the player
	 */
	void copy(Player p, int quantity) {
		ItemStack duplicate = p.getInventory().getItemInMainHand().clone();
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
	}

	/**
	 * Unsigns a book in the specified player's hand.
	 *
	 * @param p the player who triggers the unsign event
	 */
	void unsign(Player p) {
		ItemStack unsign = p.getInventory().getItemInMainHand();
		if (unsign.getType() != Material.WRITTEN_BOOK
				&& unsign.getType() != Material.WRITABLE_BOOK) {
			return;
		}
		BookMeta unsignMeta = (BookMeta) unsign.getItemMeta();
		unsignMeta.setTitle(null);
		unsign.setItemMeta(unsignMeta);
		unsign.setType(Material.WRITABLE_BOOK);
	}

	/**
	 *
	 * @param p the player who triggers the event
	 * @param pageNumber the place the page is to be inserted
	 * @param text the text that goes on the page
	 *
	 * @return whether the user was successful in their attempt at inserting a page
	 */
	boolean insertPageAt(Player p, String pageNumber, String text) {
		if (!p.getInventory().getItemInMainHand().getType().equals(Material.WRITABLE_BOOK)) {
			p.sendMessage(plugin.getMessages().get("FAILURE_EDIT_NOBAQ"));
			return false;
		}
		ItemStack book = p.getInventory().getItemInMainHand();
		BookMeta bm = (BookMeta) book.getItemMeta();
		List<String> pages = new ArrayList<String>(bm.getPages());
		try {
			pages.add(Integer.parseInt(pageNumber), text);
			bm.setPages(pages);
		} catch (NumberFormatException e) {
			p.sendMessage(plugin.getMessages().get("FAILURE_USAGE") + plugin.getMessages().get("USAGE_EDIT_ADDPAGE"));
			return false;
		} catch (IndexOutOfBoundsException e) {
			p.sendMessage(plugin.getMessages().get("FAILURE_EDIT_INVALIDNUMBER"));
			return false;
		} catch (Exception e) {
			e.printStackTrace();
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
	boolean deletePageAt(Player p, String pageNumber) {
		if (!p.getInventory().getItemInMainHand().getType().equals(Material.WRITABLE_BOOK)) {
			p.sendMessage(plugin.getMessages().get("FAILURE_EDIT_NOBAQ"));
			return false;
		}
		ItemStack book = p.getInventory().getItemInMainHand();
		BookMeta bm = (BookMeta) book.getItemMeta();
		List<String> pages = new ArrayList<String>(bm.getPages());
		try {
			pages.remove(Integer.parseInt(pageNumber) + 1);
			bm.setPages(pages);
		} catch (NumberFormatException e) {
			p.sendMessage(plugin.getMessages().get("FAILURE_USAGE") + plugin.getMessages().get("USAGE_EDIT_DELPAGE"));
			return false;
		} catch (IndexOutOfBoundsException e) {
			p.sendMessage(plugin.getMessages().get("FAILURE_EDIT_INVALIDNUMBER"));
			return false;
		} catch (Exception e) {
			e.printStackTrace();
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
	boolean canObtainMap(Player p) {
		Inventory inv = p.getInventory();
		if (p.hasPermission("booksuite.book.free") || p.getGameMode().equals(GameMode.CREATIVE)) {
			if (inv.firstEmpty() == -1 && p.getInventory().getItemInMainHand().getAmount() == 64) {
				p.sendMessage(plugin.getMessages().get("FAILURE_SPACE"));
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
				p.sendMessage(plugin.getMessages().get("FAILURE_COPY_MAP"));
				return false;
			}
		}
		if (inv.firstEmpty() == -1) {
			inv.addItem(new ItemStack(Material.PAPER, 9));
			p.sendMessage(plugin.getMessages().get("FAILURE_SPACE"));
			return false;
		} else {
			return true;
		}
	}

	/**
	 * tests if a given block is an inverted stair block
	 *
	 * @param block the block to be tested
	 *
	 * @return whether the block is an inverted stair
	 */
	public boolean isInvertedStairs(Block block) {
		if (!block.getType().name().contains("STAIRS")) {
			return false;
		}
		BlockData data = block.getBlockData();
		if (!(data instanceof Bisected)) {
			return false;
		}
		return ((Bisected) data).getHalf() == Bisected.Half.TOP;
	}

	/**
	 * Parses Book Markup Language (BML)
	 *
	 * @param text the <code>String</code> to parse
	 *
	 * @return the <code>String</code> after parsing
	 */
	String parseBML(String text) {
		if (text == null) {
			return null;
		}
		text = text.replaceAll("[<\\[][Ii]([Tt][Aa][Ll][Ii][Cc][Ss]?)?[>\\]]", ChatColor.ITALIC.toString());
		text = text.replaceAll("[<\\[][Bb]([Oo][Ll][Dd])?[>\\]]", ChatColor.BOLD.toString());
		text = text.replaceAll("[<\\[][Uu]([Nn][Dd][Ee][Rr][Ll][Ii][Nn][Ee][Dd]?)?[>\\]]", ChatColor.UNDERLINE.toString());
		text = text.replaceAll("[<\\[]([Ss]([Tt][Rr][Ii][Kk][Ee]([Tt][Hh][Rr][Oo][Uu][Gg][Hh])?)?|[Dd][Ee][Ll])[>\\]]", ChatColor.STRIKETHROUGH.toString());
		text = text.replaceAll("[<\\[]([Mm]([Aa][Gg][Ii][Cc])?|[Oo][Bb][Ff]([Uu][Ss][Cc][Aa][Tt][Ee][Dd]?)?)[>\\]]", ChatColor.MAGIC.toString());

		text = text.replaceAll("[<\\[][Cc][Oo][Ll][Oo][Rr]=", "<");
		text = text.replaceAll("[<\\[][Bb][Ll][Aa][Cc][Kk][>\\]]", ChatColor.BLACK.toString());
		text = text.replaceAll("[<\\[][Dd][Aa][Rr][Kk]_?[Bb][Ll][Uu][Ee][>\\]]", ChatColor.DARK_BLUE.toString());
		text = text.replaceAll("[<\\[][Dd][Aa][Rr][Kk]_?[Gg][Rr][Ee][Ee][Nn][>\\]]", ChatColor.DARK_GREEN.toString());
		text = text.replaceAll("[<\\[][Dd][Aa][Rr][Kk]_?[Aa][Qq][Uu][Aa][>\\]]", ChatColor.DARK_AQUA.toString());
		text = text.replaceAll("[<\\[][Dd][Aa][Rr][Kk]_?[Rr][Ee][Dd][>\\]]", ChatColor.DARK_RED.toString());
		text = text.replaceAll("[<\\[]([Dd][Aa][Rr][Kk]_?)?[Pp][Uu][Rr][Pp][Ll][Ee][>\\]]", ChatColor.DARK_PURPLE.toString());
		text = text.replaceAll("[<\\[]([Dd][Aa][Rr][Kk]_?)?[Gg][Oo][Ll][Dd][>\\]]", ChatColor.GOLD.toString());
		text = text.replaceAll("[<\\[][Gg][Rr][AEae][Yy][>\\]]", ChatColor.GRAY.toString());
		text = text.replaceAll("[<\\[][Dd][Aa][Rr][Kk]_?[Gg][Rr][AEae][Yy][>\\]]", ChatColor.DARK_GRAY.toString());
		text = text.replaceAll("[<\\[]([Ii][Nn][Dd][Ii][Gg][Oo]|(([Ll]|[Bb][Rr])[Ii][Gg][Hh][Tt]_?)?[Bb][Ll][Uu][Ee])[>\\]]", ChatColor.BLUE.toString());
		text = text.replaceAll("[<\\[](([Ll]|[Bb][Rr])[Ii][Gg][Hh][Tt]_?)?[Gg][Rr][Ee][Ee][Nn][>\\]]", ChatColor.GREEN.toString());
		text = text.replaceAll("[<\\[][Aa][Qq][Uu][Aa][>\\]]", ChatColor.AQUA.toString());
		text = text.replaceAll("[<\\[](([Ll]|[Bb][Rr])[Ii][Gg][Hh][Tt]_?)?[Rr][Ee][Dd][>\\]]", ChatColor.RED.toString());
		text = text.replaceAll("[<\\[]((([Ll]|[Bb][Rr])[Ii][Gg][Hh][Tt]_?)?[Pp][Uu][Rr][Pp][Ll][Ee]|[Pp][Ii][Nn][Kk]|[Mm][Aa][Gg][Ee][Nn][Tt][Aa])[>\\]]", ChatColor.LIGHT_PURPLE.toString());
		text = text.replaceAll("[<\\[][Yy][Ee][Ll][Ll][Oo][Ww][>\\]]", ChatColor.YELLOW.toString());
		text = text.replaceAll("[<\\[][Ww][Hh][Ii][Tt][Ee][>\\]]", ChatColor.WHITE.toString());

		text = ChatColor.translateAlternateColorCodes('&', text);

		text = text.replaceAll("[<\\[]/([Ii]([Tt][Aa][Ll][Ii][Cc][Ss]?)?|[Bb]([Oo][Ll][Dd])?|[Uu]([Nn][Dd][Ee][Rr][Ll][Ii][Nn][Ee][Dd]?)?"
				+ "|[Ss]([Tt][Rr][Ii][Kk][Ee]([Tt][Hh][Rr][Oo][Uu][Gg][Hh])?)?|[Dd][Ee][Ll]|[Ff][Oo][Rr][Mm][Aa][Tt]"
				+ "|[Mm]([Aa][Gg][Ii][Cc])?|[Oo][Bb][Ff]([Uu][Ss][Cc][Aa][Tt][Ee][Dd]?)?)[>\\]]", ChatColor.RESET.toString());
		text = text.replaceAll("[<\\[]/[Cc][Oo][Ll][Oo][Rr][>\\]]", ChatColor.WHITE.toString());
		text = text.replaceAll("[<\\[][Hh][Rr][>\\]]", "\n-------------------\n");
		text = text.replaceAll("[<\\[]([Nn]|[Bb][Rr])[>\\]]", "\n");
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
	String addColor(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	/**
	 * Lists book files in directory
	 *
	 * @param directory the directory
	 * @param p the <code>Player</code> to obtain file list
	 */
	void listBookFilesIn(String directory, Player p) {
		final File file = new File(directory);
		if (!file.exists()) {
			p.sendMessage(plugin.getMessages().get("FAILURE_LIST_NOBOOKS"));
			return;
		}
		File[] publicBooks = file.listFiles();
		if (publicBooks == null || publicBooks.length == 0) {
			p.sendMessage(plugin.getMessages().get("FAILURE_LIST_NOBOOKS"));
			return;
		}
	StringBuilder bookList = new StringBuilder();
		p.sendMessage(plugin.getMessages().get("SUCCESS_LIST"));
		for (File bookFile : publicBooks) {
			bookList.append(bookFile.getName().replace(".book", "")).append(", ");
			// Maximum allowed characters in a server-to-client chat message is 32767.
			// In the event that people actively try to mess things up or
			// there are an obscene amount of books, we'll cut it off.
			if (bookList.length() > 32500) {
				p.sendMessage(ChatColor.DARK_GREEN
						+ bookList.substring(0, bookList.length() - 2));
				bookList = new StringBuilder();
			}
		}
		p.sendMessage(ChatColor.DARK_GREEN + bookList.substring(0, bookList.length() - 2));
	}

	boolean isAuthor(Player p, String author) {
		return author == null || p.getName().equals(author) || plugin.getConfig().getBoolean("enable-aliases") && author.contains(p.getDisplayName());
	}
}
