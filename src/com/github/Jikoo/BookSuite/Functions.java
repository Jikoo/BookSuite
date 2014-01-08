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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Functions {
	private static Functions instance = null;
	private final char SECTION_SIGN = '\u00A7';
	private final int[] ACCEPTABLE = { 53, 67, 108, 109, 114, 128, 134, 135, 136, 156 };

	/**
	 * master method for checking if the player can obtain the books
	 * 
	 * @param p
	 *            the player attempting to obtain the book
	 * @return whether the player can obtain the book
	 */
	public boolean canObtainBook(Player p) {
		Inventory inv = p.getInventory();

		if (p.hasPermission("booksuite.book.free") || p.getGameMode().equals(GameMode.CREATIVE)) {
			if (!hasRoom(p)) {
				p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_SPACE"));
				return false;
			}
			return true;
		}
		String supplies = checkBookSupplies(inv);
		if (supplies == null) {
			inv.removeItem(new ItemStack(Material.INK_SACK, 1));
			inv.removeItem(new ItemStack(Material.BOOK, 1));
			if (!hasRoom(p)) {
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
	 * 
	 * @param p
	 *            the player
	 * @return whether p (the player) has enough room to store a book
	 */
	public boolean hasRoom(Player p) {
		return p.getInventory().firstEmpty() != -1 || hasStackingRoom(p);
	}

	/**
	 * HELPER FUNCTION for hasRoom
	 * 
	 * @param p
	 *            the player
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
				if (e.getValue().getItemMeta().equals(p.getItemInHand().getItemMeta())) {
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
			return null;
		} else if (inv.contains(Material.INK_SACK)) {
			return BookSuite.getInstance().msgs.get("FAILURE_COPY_BOOK");
		} else if (inv.contains(Material.BOOK)) {
			return BookSuite.getInstance().msgs.get("FAILURE_COPY_INK");
		}
		return BookSuite.getInstance().msgs.get("FAILURE_COPY_BOTH");
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
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_PERMISSION_COPY_OTHER"));
		else
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_PERMISSION_COPY"));
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
		if (p.hasPermission("booksuite.command.copy") && (a.equals(null) || a.equals(p.getName())))
			return true;
		else if (p.hasPermission("booksuite.command.copy")) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_PERMISSION_COPY_OTHER"));
			return false;
		} else
			return false;
	}

	/**
	 * 
	 * copies the book that the player is currently holding
	 * 
	 * @param p
	 *            the player
	 */
	@SuppressWarnings("deprecation")
	public void copy(Player p) {
		if (p.hasPermission("booksuite.copy.stack")
				&& !p.getItemInHand().getType().equals(Material.MAP)) {
			if (p.getItemInHand().getAmount() == 64) {
				HashMap<Integer, ? extends ItemStack> allBooks = p.getInventory().all(
						p.getItemInHand().getType());
				if (allBooks.size() == 1) {
					newDuplicate(p);
				} else {
					boolean copiedSuccessfully = false;
					for (Entry<Integer, ? extends ItemStack> e : allBooks.entrySet()) {
						if (e.getValue().getItemMeta().equals(p.getItemInHand().getItemMeta())) {
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
	 * Helper method for copying. Adds an <code>ItemStack</code> of size 1 to the specified player's inventory.
	 * 
	 * @param p
	 *            the <code>Player</code> in whose inventory the duplication will be done
	 */
	private void newDuplicate(Player p) {
		ItemStack duplicate = p.getItemInHand().clone();
		duplicate.setAmount(1);
		p.getInventory().addItem(duplicate);
	}

	/**
	 * 
	 * @param p
	 *            the player who triggers the unsign event
	 * @return whether the unsigning was successful
	 */
	public boolean unsign(Player p) {
		ItemStack unsign = p.getItemInHand();
		if (!unsign.getType().equals(Material.WRITTEN_BOOK)
				&& !unsign.getType().equals(Material.BOOK_AND_QUILL)) {
			return false;
		}
		BookMeta unsignMeta = (BookMeta) unsign.getItemMeta();
		unsignMeta.setTitle(null);
		if (unsign.getType().equals(Material.BOOK_AND_QUILL))
			unsignMeta.setAuthor(null);
		unsign.setItemMeta(unsignMeta);
		unsign.setType(Material.BOOK_AND_QUILL);
		return true;
	}

	/**
	 * 
	 * @param p
	 *            the player who triggers the event
	 * @param newAuthor
	 *            the name of the author to set as the book's author
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
	 * @param p
	 *            the player who triggers the event
	 * @param newTitle
	 *            the new title of the book to be set
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
	 * @param p
	 *            the player who triggers the event
	 * @param pageNumber
	 *            the place the page is to be inserted
	 * @param text
	 *            the text that goes on the page
	 * @return whether the user was successful in their attempt at
	 *         inserting a page
	 */
	public boolean insertPageAt(Player p, String pageNumber, String text) {
		if (!p.getItemInHand().getType().equals(Material.BOOK_AND_QUILL)) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_EDIT_NOBAQ"));
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
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_USAGE") + BookSuite.getInstance().msgs.get("USAGE_EDIT_ADDPAGE"));
			return false;
		} catch (IndexOutOfBoundsException e1) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_EDIT_INVALIDNUMBER"));
			return false;
		} catch (Exception e2) {
			BSLogger.err(e2);
		}
		book.setItemMeta(bm);
		return true;
	}

	/**
	 * 
	 * @param p
	 *            the player who triggers the event
	 * @param pageNumber
	 *            the page to be deleted
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
		} catch (NumberFormatException e) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_USAGE") + BookSuite.getInstance().msgs.get("USAGE_EDIT_DELPAGE"));
			return false;
		} catch (IndexOutOfBoundsException e1) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_EDIT_INVALIDNUMBER"));
			return false;
		} catch (Exception e2) {
			BSLogger.err(e2);
		}
		book.setItemMeta(bm);
		return true;
	}

	/**
	 * 
	 * @param p
	 *            the player who triggered the event and whom is to be tested
	 *            for this property
	 * @return whether p (the player) can obtain a map
	 */
	public boolean canObtainMap(Player p) {
		if (p.hasPermission("booksuite.copy.map")) {
			Inventory inv = p.getInventory();
			if (p.hasPermission("booksuite.book.free") || p.getGameMode().equals(GameMode.CREATIVE)) {
				if (inv.firstEmpty() == -1 && p.getItemInHand().getAmount() == 64) {
					p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_SPACE"));
					return false;
				} else
					return true;
			} else if (inv.contains(new ItemStack(Material.PAPER, 9))) {
				inv.remove(new ItemStack(Material.PAPER, 9));
				if (inv.firstEmpty() == -1) {
					inv.addItem(new ItemStack(Material.PAPER, 9));
					p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_SPACE"));
					return false;
				} else
					return true;
			} else {
				p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_COPY_MAP"));
				return false;
			}
		} else {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_PERMISSION_COPY"));
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
	@SuppressWarnings("deprecation")
	public boolean isInvertedStairs(Block b) {
		for (int i : ACCEPTABLE)
			if (i == b.getTypeId())
				return b.getData() > 3;
		return false;
	}

	/**
	 * 
	 * @param is
	 *            the stack of stairs to be tested
	 * @return whether the item is an acceptable type of stair
	 */
	@SuppressWarnings("deprecation")
	public boolean isCorrectStairType(ItemStack is) {
		for (int i : ACCEPTABLE)
			if (i == is.getTypeId())
				return true;
		return false;
	}

	/**
	 * 
	 * @param p
	 *            the player who triggered the event
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
	 * @param blockToCheck
	 *            the block to check
	 * @return whether this block is a stair or crafting table part of a
	 *         printing press
	 */
	public boolean isPrintingPress(Block blockToCheck) {
		if (!BookSuite.getInstance().getConfig().getBoolean("enable-printing-presses")) {
			return false;
		}
		if (blockToCheck.getType().equals(Material.WORKBENCH)) {
			if (isInvertedStairs(blockToCheck.getRelative(BlockFace.UP))) {
				return true;
			}
		}
		if (isInvertedStairs(blockToCheck)) {
			if (blockToCheck.getRelative(BlockFace.DOWN).getType().equals(Material.WORKBENCH)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param clicked
	 *            the clicked block
	 * @param clickedFace
	 *            the face of the block that was clicked
	 * @param itemInHand
	 *            the item in the hand of the player
	 * @param p
	 *            the player who clicked
	 * @return whether the player is allowed and has met the criteria for
	 *         creating a printing press
	 */
	public boolean canMakePress(Block clicked, BlockFace clickedFace, ItemStack itemInHand, Player p) {
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
	 * @param clicked
	 *            the block that was clicked
	 * @param itemInHand
	 *            the item the user was holding when the event was triggered
	 * @return whether the user who triggered the event is able to erase this
	 *         book (note: permissions are factored in later)
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
	 * @param clicked
	 *            the block that was clicked
	 * @return whether the block is a sign or chest that is part of a mailbox
	 */
	public boolean isMailBox(Block clicked) {
		if (!BookSuite.getInstance().getConfig().getBoolean("enable-mail")) {
			return false;
		}
		Sign sign = null;
		if (clicked.getState() instanceof Sign) {
			sign = (Sign) clicked.getState();
		} else if (clicked.getType().equals(Material.CHEST)) {
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
	 * 
	 * TODO: MAKE THIS WORK
	 * 
	 * @param clicked
	 *            the block that was clicked
	 * @param clicker
	 *            the player who clicked the block
	 * @return whether block that the player clicked is part of a library
	 */
	public boolean isLibrary(Block clicked, Player clicker) {
		return false;
	}

	/**
	 * Parses file text.
	 * 
	 * @param text
	 *            the <code>String</code> to parse
	 * @return the <code>String</code> after parsing
	 */
	public String parseBML(String text) {
		text = text.replaceAll("(<|\\[)i(talic(s)?)?(>|\\])", SECTION_SIGN + "o");
		text = text.replaceAll("(<|\\[)b(old)?(>|\\])", SECTION_SIGN + "l");
		text = text.replaceAll("(<|\\[)u(nderline)?(>|\\])", SECTION_SIGN + "n");
		text = text.replaceAll("(<|\\[)(s(trike)?|del)(>|\\])", SECTION_SIGN + "m");
		text = text.replaceAll("(<|\\[)(m(agic)?|obf(uscate(d)?)?)(>|\\])", SECTION_SIGN + "k");

		text = text.replaceAll("(<|\\[)color=", "<");
		text = text.replaceAll("(<|\\[)black(>|\\])", SECTION_SIGN + "0");
		text = text.replaceAll("(<|\\[)dark_?blue(>|\\])", SECTION_SIGN + "1");
		text = text.replaceAll("(<|\\[)dark_?green(>|\\])", SECTION_SIGN + "2");
		text = text.replaceAll("(<|\\[)dark_?aqua(>|\\])", SECTION_SIGN + "3");
		text = text.replaceAll("(<|\\[)dark_?red(>|\\])", SECTION_SIGN + "4");
		text = text.replaceAll("(<|\\[)(purple|magenta)(>|\\])", SECTION_SIGN + "5");
		text = text.replaceAll("(<|\\[)gold(>|\\])", SECTION_SIGN + "6");
		text = text.replaceAll("(<|\\[)gr[ea]y(>|\\])", SECTION_SIGN + "7");
		text = text.replaceAll("(<|\\[)dark_?gr[ea]y(>|\\])", SECTION_SIGN + "8");
		text = text.replaceAll("(<|\\[)(indigo|(light_?)?blue)(>|\\])", SECTION_SIGN + "9");
		text = text.replaceAll("(<|\\[)(light_?|bright_?)?green(>|\\])", SECTION_SIGN + "a");
		text = text.replaceAll("(<|\\[)aqua(>|\\])", SECTION_SIGN + "b");
		text = text.replaceAll("(<|\\[)(light_?)?red(>|\\])", SECTION_SIGN + "c");
		text = text.replaceAll("(<|\\[)pink(>|\\])", SECTION_SIGN + "d");
		text = text.replaceAll("(<|\\[)yellow(>|\\])", SECTION_SIGN + "e");
		text = text.replaceAll("(<|\\[)white(>|\\])", SECTION_SIGN + "f");

		text = text.replaceAll("&([a-fk-orA-FK-OR0-9])", SECTION_SIGN + "$1");

		text = text.replaceAll("(<|\\[)/(i(talic(s)?)?|b(old)?|u(nderline)?|s(trike)?"
				+ "|del|format|m(agic)?|obf(uscate(d)?)?)(>|\\])", SECTION_SIGN + "r");
		text = text.replaceAll("(<|\\[)/color(>|\\])", SECTION_SIGN + "0");
		text = text.replaceAll("(<|\\[)hr(>|\\])", "\n-------------------\n");
		text = text.replaceAll("(<|\\[)(n|br)(>|\\])", "\n");
		text = text.replaceAll("(" + SECTION_SIGN + "r)+", SECTION_SIGN + "r");
		return text;
	}

	/**
	 * Lists book files in directory
	 * 
	 * @param directory
	 *            the directory
	 * @param p
	 *            the <code>Player</code> to obtain file list
	 */
	public void listBookFilesIn(String directory, Player p) {
		final File file = new File(directory);
		if (!file.exists()) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_LIST_NOBOOKS"));
			file.mkdirs();
			return;
		}
		File[] publicBooks = file.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (new File(dir, name).isDirectory()) {
					return false;
				}
				return true;
			}
		});
		File[] privateBooks = null;
		if (BookSuite.getInstance().getConfig()
				.getBoolean("allow-private-saving")) {
			File privateFile = new File(file, p.getName());
			if (privateFile.exists()) {
				privateBooks = privateFile.listFiles();
			}
		}
		if (publicBooks == null && privateBooks == null) {
			p.sendMessage(BookSuite.getInstance().msgs.get("FAILURE_LIST_NOBOOKS"));
			return;
		}
		String bookList = new String();
		if (publicBooks != null && publicBooks.length != 0) {
			p.sendMessage(BookSuite.getInstance().msgs.get("SUCCESS_LIST_PRIVATE"));
			for (File bookFile : publicBooks) {
				bookList += bookFile.getName().replace(".book", "") + ", ";
				// Maximum allowed characters in a server-to-client chat message
				// is 32767.
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
			p.sendMessage(ChatColor.DARK_GREEN
					+ bookList.substring(0, bookList.length() - 3));
		}
		if (privateBooks != null && privateBooks.length != 0) {
			bookList = new String();
			p.sendMessage(BookSuite.getInstance().msgs.get("SUCCESS_LIST_PRIVATE"));
			for (File bookFile : privateBooks) {
				bookList += bookFile.getName().replace(".book", "") + ", ";

				if (bookList.length() > 32500) {
					p.sendMessage(ChatColor.DARK_GREEN
							+ bookList.substring(0, bookList.length() - 2));
					bookList = new String();
				}
			}
		}
	}

	public boolean isAuthor(Player p, String author) {
		ArrayList<String> aliases = BookSuite.getInstance().alias.getAliases(p);
		for (String s : parseAuthors(author)) {
			if (aliases.contains(s)) {
				return true;
			}
		}
		return false;
	}

	public String[] parseAuthors(String authors) {
		return authors.replaceAll(",? and", ", ").split("(" + ChatColor.GRAY + ")?,\\s+");
	}

	public String getAuthors(String[] old, String author) {
		StringBuilder sb = new StringBuilder();
		if (old != null) {
			for (int i = 0; i < old.length; i++) {
				sb.append(old[i]).append(ChatColor.GRAY).append(", ");
			}
			sb.deleteCharAt(sb.lastIndexOf(",")).append("and ");
		}
		return sb.append(author).toString();
	}

	/**
	 * Adds an author to a book. Adds/edits fake lore if not a signing event,
	 * otherwise removes existing faked lore
	 * 
	 * @param bm
	 *            the <code>BookMeta</code> to edit
	 * @param author
	 *            name to add as an author
	 * @param signing
	 *            true if the book is being converted into a written book
	 */
	public BookMeta addAuthor(BookMeta bm, String oldAuthors, Player author, boolean signing) {
		String newAuthors = BookSuite.getInstance().alias.getActiveAlias(author);
		boolean isCredited = false;
		if (oldAuthors != null) {
			isCredited = this.isAuthor(author, oldAuthors);
			if (!isCredited) {
				newAuthors = this.getAuthors(this.parseAuthors(oldAuthors), newAuthors);
			}
		}
		bm.setAuthor(isCredited ? oldAuthors : newAuthors);

		ArrayList<String> lore = bm.hasLore() ? (ArrayList<String>) bm.getLore() : null;
		if (bm.hasLore() && lore.get(0).equals(ChatColor.GRAY + "by " + oldAuthors)) {
			if (signing) {
				lore.remove(0);
				if (lore.isEmpty()) {
					lore = null;
				}
			} else if (!isCredited) {
				lore.set(0,
						new StringBuilder().append(ChatColor.GRAY).append("by ").append(newAuthors)
								.toString());
			}
		} else {
			lore = new ArrayList<String>();
			lore.add(new StringBuilder().append(ChatColor.GRAY).append("by ").append(isCredited ? oldAuthors : newAuthors)
					.toString());
			if (bm.hasLore()) {
				lore.addAll(bm.getLore());
			}
		}
		bm.setLore(lore);

		return bm;
	}

	protected void disable() {
		instance = null;
	}

	/**
	 * 
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
