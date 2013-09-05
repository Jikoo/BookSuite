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
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * FileManager handles most generic file-related functions of BookSuite.
 * 
 * @author Jikoo
 * @author tmathmeyer
 */
public class FileManager {

	/**
	 * Makes a <code>BookMeta</code> from text. Text is read from a plaintext
	 * <code>File</code> file stored in the directory location. If the file is
	 * read from a <code>URL</code>, additional regex is applied to prevent
	 * special characters appearing incorrectly.
	 * 
	 * @param p
	 *            the <code>Player</code> attempting to obtain a book
	 * @param file
	 *            the name of the <code>File</code> to read
	 * @param location
	 *            the directory of the <code>File</code>
	 * @param isURL
	 *            the type of import
	 * @return the <code>BookMeta</code> created.
	 */
	public BookMeta makeBookMetaFromText(Player p, String file,
			String location, boolean isURL) {
		BookMeta text = (BookMeta) new ItemStack(Material.WRITTEN_BOOK, 1)
				.getItemMeta();
		boolean isBookText = false;
		if (!isURL)
			isBookText = true;

		try {
			Scanner s;
			if (file.contains("."))
				s = new Scanner(new File(location, file));
			else
				s = new Scanner(new File(location, file + ".book"));
			String page = "";
			while (s.hasNext()) {
				String line = s.nextLine();

				// pastebin support section
				if (location.contains("temp")) {
					line = line.replaceAll("(<li class=\").*(\">)", "")
							.replace("</li>", "");
					line = line.replaceAll("(<div class=\").*(\">)", "")
							.replace("</div>", "");
					line = line.replace("&lt;", "<").replace("&gt;", ">");
					line = line.replace("&nbsp", "<n>");
				}

				if (line.contains("<book>")) {
					isBookText = true;
					line = line.replace("<book>", "");
				}
				if (isBookText) {
					if (line.contains("</book>")) {
						break;
					}
					if (line.length() >= 2 && line.substring(0, 2).equals("//")) {
						// do nothing, this line is a book comment
					} else if (line.contains("<author>")
							&& (!isURL || p
									.hasPermission("booksuite.command.import.other"))) {
						text.setAuthor(line.replace("<author>", "").replace(
								"</author>", ""));
					} else if (line.contains("<title>")) {
						text.setTitle(line.replace("<title>", "")
								.replace("</title>", "").replace("<br>", ""));
					} else if (line.contains("<page>")) {
						page = "";
					} else if (line.contains("</page>")) {
						text.addPage(BookSuite.getInstance().functions.parseBML(page));
					} else {
						page += line + "<n>";
					}
				}
			}
			s.close();
			if (!text.hasAuthor())
				text.setAuthor(p.getName());
			return text;
		} catch (Exception e) {
			BSLogger.err(e);
			return null;
		}
	}

	/**
	 * Makes an <code>ItemStack</code> from text. Text is read from a plaintext
	 * <code>File</code> filename stored in the directory directory.
	 * 
	 * @param directory
	 *            the directory of the <code>File</code>
	 * @param filename
	 *            the name of the <code>File</code> to read
	 * @return the <code>ItemStack</code> created
	 */
	public ItemStack makeItemStackFromFile(String directory, String filename) {
		ItemStack is = new ItemStack(3, 1);
		ItemMeta im = is.getItemMeta();
		try {
			File itemFile = new File(directory, filename + ".item");
			Scanner s = new Scanner(itemFile);
			List<String> lore = new ArrayList<String>();
			boolean handlingEnchants = false;
			while (s.hasNext()) {
				String line = s.nextLine();

				if (line.contains("<TypeID>")) {
					is.setType(Material.matchMaterial(line.replaceAll("<Type>",
							"").replaceAll("</Type>", "")));
				} else if (line.contains("<Amount>")) {
					is.setAmount(Integer.parseInt(line.replaceAll("<Amount>",
							"").replaceAll("</Amount>", "")));
				} else if (line.contains("<Durability>")) {
					is.setDurability((short) Integer
							.parseInt(line.replaceAll("<Durability>", "")
									.replaceAll("</Durability>", "")));
				} else if (line.contains("<Lore>")) {
					lore.clear();
				} else if (line.contains("</Lore>")) {
					im.setLore(lore);
				} else if (line.contains("<DisplayName>")) {
					im.setDisplayName(line.replaceAll("<DisplayName>", "")
							.replaceAll("</DisplayName>", ""));
				} else if (line.contains("<Enchantments>")) {
					handlingEnchants = true;
				} else if (line.contains("</Enchantments>")) {
					break;
				} else if (handlingEnchants) {
					String[] enchant = line.split(":");
					im.addEnchant(
							Enchantment.getById(Integer.parseInt(enchant[0])),
							Integer.parseInt(enchant[1]), true);
				} else {
					lore.add(line);
				}
			}
			s.close();
			is.setItemMeta(im);
			return is;
		} catch (Exception e) {
			BSLogger.err(e);
			im.setDisplayName("Item file error! My condolences.");
			is.setItemMeta(im);
			return is;
		}
	}

	/**
	 * Makes a plaintext <code>File</code> filename in directory directory from
	 * the contents of <code>BookMeta</code> bm.
	 * 
	 * @param bm
	 *            the <code>BookMeta</code>
	 * @param directory
	 *            the directory to write to
	 * @param filename
	 *            the <code>File</code> to write
	 * @return <code>true</code>, if successful
	 */
	public boolean makeFileFromBookMeta(BookMeta bm, String directory,
			String filename) {

		try {
			File bookLocation = new File(directory);
			if (!bookLocation.exists())
				bookLocation.mkdirs();
			File bookFile = new File(bookLocation, filename + ".book");
			if (!bookFile.exists()) {
				bookFile.createNewFile();
			} else {
				return false;
			}
			FileWriter file = new FileWriter(bookFile);
			file.write("<book>\n");
			file.append("<author>" + bm.getAuthor() + "</author>\n");
			file.append("<title>" + bm.getTitle() + "</title>\n");
			for (int i = 1; i <= bm.getPageCount(); i++)
				file.append("<page>\n" + bm.getPage(i) + "\n</page>\n");
			file.append("</book>");
			file.close();
			return true;
		} catch (Exception e) {
			BSLogger.err(e);
			return true;
		}
	}

	/**
	 * Makes a plaintext <code>File</code> filename in directory directory from
	 * the contents of <code>ItemStack</code> is.
	 * 
	 * @param is
	 *            the <code>ItemStack</code>
	 * @param directory
	 *            the directory to write to
	 * @param filename
	 *            the name of the <code>File</code> to write
	 * @return <code>true</code>, if successful
	 */
	public boolean makeFileFromItemStack(ItemStack is, String directory,
			String filename) {
		try {
			File itemLocation = new File(directory);
			if (!itemLocation.exists())
				itemLocation.mkdirs();
			File itemFile = new File(itemLocation, filename + ".item");
			if (!itemFile.exists()) {
				itemFile.createNewFile();
			} else {
				return false;
			}
			FileWriter file = new FileWriter(itemFile);
			file.write("<Type>" + is.getType().name() + "</Type>\n");
			file.append("<Amount>" + is.getAmount() + "</Amount>");
			file.append("<Durability>" + is.getDurability() + "</Durability>\n");
			if (is.hasItemMeta()) {
				ItemMeta im = is.getItemMeta();
				if (im.hasDisplayName())
					file.append("<DisplayName>" + im.getDisplayName()
							+ "</DisplayName>\n");
				if (im.hasLore()) {
					List<String> loreList = im.getLore();
					file.append("<Lore>\n");
					for (int i = 0; i < loreList.size(); i++)
						file.append(loreList.get(i) + "\n");
					file.append("</Lore>\n");
				}
				if (im.hasEnchants()) {
					file.append("<Enchantments>\n");
					for (Enchantment e : Enchantment.values()) {
						file.append(e.getId() + ":" + im.getEnchantLevel(e)
								+ "\n");
					}
					file.append("</Enchantments>");
				}
			}
			file.close();
			return false;
		} catch (Exception e) {
			BSLogger.err(e);
			return true;
		}
	}

	/**
	 * Append mail index.
	 * 
	 * @param directory
	 *            the directory
	 * @param appendText
	 *            the <code>String</code> to append to the mail index
	 * @return <code>true</code>, if successful
	 */
	public boolean appendMailIndex(String directory, String appendText) {
		try {
			File indexLocation = new File(directory);
			if (!indexLocation.exists())
				indexLocation.mkdirs();
			File indexFile = new File(indexLocation, "index.bsm");
			FileWriter index;
			if (indexFile.exists()) {
				index = new FileWriter(indexFile);
				index.append(appendText + "\n");
			} else {
				indexFile.createNewFile();
				index = new FileWriter(indexFile);
				index.write(appendText + "\n");
			}
			index.close();
			return true;
		} catch (Exception e) {
			BSLogger.err(e);
			return false;
		}

	}

	/**
	 * Removes mail.
	 * 
	 * @param directory
	 *            the directory
	 * @param mail
	 *            the name of the mail <code>File</code>
	 * @return <code>true</code>, if successful
	 */
	public boolean removeMail(String directory, String mail) {
		try {
			File indexFile = new File(directory, "index.bsm");
			if (!indexFile.exists())
				return false;
			Scanner s = new Scanner(indexFile);
			String indexContents = "";

			while (s.hasNextLine()) {
				String line = s.nextLine();
				if (!line.equals(mail)) {
					indexContents += line + "\n";

				}
			}
			s.close();
			FileWriter writer = new FileWriter(indexFile);
			writer.write(indexContents);
			writer.close();
			delete(directory, mail + ".book");
		} catch (Exception e) {
			BSLogger.err(e);
			return false;
		}
		return true;
	}

	/**
	 * Deletes specified <code>File</code>
	 * 
	 * @param directory
	 *            the directory
	 * @param filename
	 *            the <code>File</code> name
	 * @return <code>true</code>, if successful
	 */
	public boolean delete(String directory, String filename) {
		File file = new File(directory, filename);
		if (!file.exists())
			return false;
		return file.delete();
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
			p.sendMessage(Msgs.FAILURE_LIST_NOBOOKS.getMessage());
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
			p.sendMessage(Msgs.FAILURE_LIST_NOBOOKS.getMessage());
			return;
		}
		String bookList = new String();
		if (publicBooks != null && publicBooks.length != 0) {
			p.sendMessage(Msgs.SUCCESS_LIST_PRIVATE.getMessage());
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
			p.sendMessage(Msgs.SUCCESS_LIST_PRIVATE.getMessage());
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

	protected void disable() {
		instance = null;
	}

	/** The <code>FileManager</code> instance. */
	private static FileManager instance;

	/**
	 * Gets the single instance of <code>FileManager</code>.
	 * 
	 * @return single instance of <code>FileManager</code>
	 */
	protected static FileManager getInstance() {
		if (instance == null)
			instance = new FileManager();
		return instance;
	}
}
