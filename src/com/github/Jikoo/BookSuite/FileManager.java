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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class FileManager {

	/**
	 * Makes a BookMeta from text. Text is read from a plaintext File file
	 * stored in the directory location. If the file is read from a URL,
	 * additional regex is applied to prevent special characters appearing
	 * incorrectly.
	 * 
	 * @param p the Player attempting to obtain a book
	 * @param file the name of the File to read
	 * @param location the directory of the File
	 * @param isURL the type of import
	 * 
	 * @return the BookMeta created.
	 */
	public BookMeta makeBookMetaFromText(CommandSender s, String fileData, boolean isURL) {
		BookMeta text = (BookMeta) new ItemStack(Material.WRITTEN_BOOK, 1).getItemMeta();
		boolean isBookText = !isURL;
		String page = "";
		for (String line : fileData.split("\n")) {

			// pastebin support section
			if (isURL) {
				line = line.replaceAll("(<li class=\").*(\">)", "").replace("</li>", "");
				line = line.replaceAll("(<div class=\").*(\">)", "").replace("</div>", "");
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
						&& (!isURL || s.hasPermission("booksuite.command.import.other"))) {
					text.setAuthor(line.replace("<author>", "").replace("</author>", ""));
				} else if (line.contains("<title>")) {
					text.setTitle(line.replace("<title>", "").replace("</title>", "").replace("<br>", ""));
				} else if (line.contains("<page>")) {
					page = "";
				} else if (line.contains("</page>")) {
					text.addPage(BookSuite.getInstance().functions.parseBML(page));
				} else {
					page += line + "<n>";
				}
			}
		}
		if (!text.hasAuthor())
			text.setAuthor(s.getName());
		return text;
	}

	public String getFileData(String directory, String file) {
		if (!file.contains(".")) {
			file += ".book";
		}
		Scanner s = null;
		StringBuilder sb = new StringBuilder();
		try {
			s = new Scanner(new File(directory, file));
			while (s.hasNextLine()) {
				sb.append(s.nextLine()).append('\n');
			}
			s.close();
			return sb.toString();
		} catch (FileNotFoundException e) {
			BSLogger.err(e);
			return null;
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	/**
	 * Makes an ItemStack from text. Text is read from a plaintext File filename
	 * stored in the directory directory.
	 * 
	 * @param directory the directory of the File
	 * @param filename the name of the File to read
	 * 
	 * @return the ItemStack created
	 */
	public ItemStack makeItemStackFromFile(String directory, String filename) {
		ItemStack is = new ItemStack(Material.DIRT, 1);
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
							Enchantment.getByName(enchant[0]),
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
	 * Makes a plaintext File filename in directory directory from the contents
	 * of BookMeta bm.
	 * 
	 * @param bm the BookMeta
	 * @param directory the directory to write to
	 * @param filename the File to write
	 * 
	 * @return true, if successful
	 */
	public boolean makeFileFromBookMeta(BookMeta bm, String directory,
			String filename) {

		try {
			File bookLocation = new File(directory);
			if (!bookLocation.exists()) {
				bookLocation.mkdirs();
			}
			if (!filename.contains(".")) {
				filename += ".book";
			}
			File bookFile = new File(bookLocation, filename);
			if (!bookFile.exists()) {
				bookFile.createNewFile();
			} else {
				return false;
			}
			FileWriter file = new FileWriter(bookFile);
			file.write("<book>\n");
			file.append("<author>" + bm.getAuthor() + "</author>\n");
			file.append("<title>" + bm.getTitle() + "</title>\n");
			for (int i = 1; i <= bm.getPageCount(); i++) {
				file.append("<page>\n" + bm.getPage(i) + "\n</page>\n");
			}
			file.append("</book>");
			file.close();
			return true;
		} catch (Exception e) {
			BSLogger.err(e);
			return true;
		}
	}

	/**
	 * Makes a plaintext File filename in directory directory from the contents
	 * of ItemStack is.
	 * 
	 * @param is the ItemStack
	 * @param directory the directory to write to
	 * @param filename the name of the File to write
	 * 
	 * @return true, if successful
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
						file.append(e.getName() + ":" + im.getEnchantLevel(e)
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
	 * Deletes specified File
	 * 
	 * @param directory the directory
	 * @param filename the File name
	 * 
	 * @return true if successful
	 */
	public boolean delete(String directory, String filename) {
		File file = new File(directory, filename);
		if (!file.exists())
			return false;
		return file.delete();
	}

	protected void disable() {
		instance = null;
	}

	/** The FileManager instance. */
	private static FileManager instance;

	/**
	 * Gets the single instance of FileManager.
	 * 
	 * @return single instance of FileManager
	 */
	protected static FileManager getInstance() {
		if (instance == null)
			instance = new FileManager();
		return instance;
	}
}
