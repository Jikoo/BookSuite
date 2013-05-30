/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn- initial API and implementation
 *     Ted Meyer - some help
 ******************************************************************************/
package com.github.Jikoo.BookSuite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
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
 * FileManager handles most generic file-related
 * functions of BookSuite.
 * @author Jikoo
 * @author tmathmeyer
 */
public class FileManager {

	private final char SECTION_SIGN = '\u00A7';


	/**
	 * Makes a BookMeta from text. Text is read from a plaintext file
	 * file stored in the directory location. If the file is read from
	 * a URL, additional regex is applied to prevent special characters
	 * appearing incorrectly.
	 *
	 * @param p the Player attempting to obtain a book
	 * @param file the name of the file to read
	 * @param location the directory of the file
	 * @param isURL the type of import
	 * @return the BookMeta created.
	 */
	public BookMeta makeBookMetaFromText(Player p, String file, String location, boolean isURL) {
		BookMeta text = (BookMeta) new ItemStack(Material.WRITTEN_BOOK, 1).getItemMeta();
		boolean isBookText = false;
		if (!isURL)
			isBookText=true;
		
		try {
			Scanner s;
			if(file.contains(".")) s = new Scanner(new File(location, file));
			else s = new Scanner(new File(location, file+".book"));
			String page = "";
			while(s.hasNext()) {
				String line = s.nextLine();
				
				//pastebin support section
				if (location.contains("temp")) {
					line=line.replaceAll("(<li class=\").*(\">)", "").replace("</li>", "");
					line=line.replaceAll("(<div class=\").*(\">)", "").replace("</div>", "");
					line=line.replace("&lt;", "<").replace("&gt;", ">");
					line=line.replace("&nbsp", "<n>");
					line=line.replaceAll("Â" + SECTION_SIGN, SECTION_SIGN + "");
				}
				
				
				if (line.contains("<book>")) {
					isBookText = true;
					line = line.replace("<book>", "");
				}
				if (isBookText){
					if(line.contains("</book>")) {
						break;
					}
					if (line.length()>=2 && line.substring(0, 2).equals("//")) {
						//do nothing, this line is a book comment
					}
					else if (line.contains("<author>") && (!isURL || p.hasPermission("booksuite.command.import.other"))) {
						text.setAuthor(line.replace("<author>", "").replace("</author>", ""));
					}
					else if (line.contains("<title>")) {
						text.setTitle(line.replace("<title>", "").replace("</title>", "").replace("<br>", ""));
					}
					else if(line.contains("<page>")) {
						page = "";
					}
					else if (line.contains("</page>")) {
						text.addPage(parseBookText(page));
					}
					else {
						page+=line+"<n>";
					}
				}
			}
			s.close();
			if (!text.hasAuthor())
				text.setAuthor(p.getName());
			return text;
		} catch (Exception ex) {
			System.err.println("[BookSuite] Error report:\nBookSuiteFileManager.makeBookMetaFromText: "+ex);
			ex.printStackTrace();
			System.err.println("[BookSuite] End error report.");
			return null;
		}
	}
	
	
	
	
	/**
	 * Makes an ItemStack from text. Text is read from a plaintext file
	 * filename stored in the directory directory.
	 *
	 * @param directory the directory of the file
	 * @param filename the name of the file to read
	 * @return the ItemStack created
	 */
	public ItemStack makeItemStackFromFile(String directory, String filename) {
		ItemStack is = new ItemStack (3, 1);
		ItemMeta im = is.getItemMeta();
		try {
			File itemFile = new File(directory, filename+".item");
			Scanner s = new Scanner(itemFile);
			List<String> lore = new ArrayList<String>();
			boolean handlingEnchants = false;
			while(s.hasNext()) {
				String line = s.nextLine();
				
				if (line.contains("<TypeID>")) {
					is.setType(Material.matchMaterial(line.replaceAll("<Type>", "").replaceAll("</Type>", "")));
				} else if (line.contains("<Amount>")) {
					is.setAmount(Integer.parseInt(line.replaceAll("<Amount>", "").replaceAll("</Amount>", "")));
				} else if (line.contains("<Durability>")) {
					is.setDurability((short)Integer.parseInt(line.replaceAll("<Durability>", "").replaceAll("</Durability>", "")));
				} else if(line.contains("<Lore>")) {
					lore.clear();
				} else if (line.contains("</Lore>")) {
					im.setLore(lore);
				} else if (line.contains("<DisplayName>")) {
					im.setDisplayName(line.replaceAll("<DisplayName>", "").replaceAll("</DisplayName>", ""));
				} else if(line.contains("<Enchantments>")) {
					handlingEnchants = true;
				} else if(line.contains("</Enchantments>")) {
					break;
				} else if (handlingEnchants) {
					String[] enchant = line.split(":");
					im.addEnchant(Enchantment.getById(Integer.parseInt(enchant[0])), Integer.parseInt(enchant[1]), true);
				} else {
					lore.add(line);
				}
			}
			s.close();
			is.setItemMeta(im);
			return is;
		} catch(Exception ex) {
			im.setDisplayName("Item file error! My condolences.");
			is.setItemMeta(im);
			return is;
		}
	}
	
	
	
	
	
	/**
	 * Makes a plaintext file filename in directory directory
	 * from the contents of BookMeta bm.
	 *
	 * @param bm the BookMeta
	 * @param directory the directory to write to
	 * @param filename the file to write
	 * @return true, if successful
	 */
	public boolean makeFileFromBookMeta(BookMeta bm, String directory, String filename) {
		
		try {
			File bookLocation = new File(directory);
			if (!bookLocation.exists())
				bookLocation.mkdirs();
			File bookFile = new File(bookLocation, filename+".book");
			if (!bookFile.exists()){
				bookFile.createNewFile();
			} else throw new FileAlreadyExistsException(bookFile.getAbsolutePath());
			FileWriter file = new FileWriter(bookFile);
			file.write("<book>\n");
			file.append("<author>"+bm.getAuthor()+"</author>\n");
			file.append("<title>"+bm.getTitle()+"</title>\n");
			for (int i = 1; i <= bm.getPageCount(); i++)
				file.append("<page>\n"+bm.getPage(i)+"\n</page>\n");
			file.append("</book>");
			file.close();
			return true;
		} catch(FileAlreadyExistsException fe) {
			return false;
		} catch(Exception e) {
			System.err.println("[BookSuite] BookSuiteFileManager.makeFileFromBookMeta: "+e);
			e.printStackTrace();
			System.err.println("[BookSuite] End error report.");
			return true;
		}
	}
	
	
	
	
	/**
	 * Makes a plaintext file filename in directory directory
	 * from the contents of ItemStack is.
	 *
	 * @param is the ItemStack
	 * @param directory the directory to write to
	 * @param filename the name of the file to write
	 * @return true, if successful
	 */
	public boolean makeFileFromItemStack(ItemStack is, String directory, String filename) {
		try {
			File itemLocation = new File(directory);
			if (!itemLocation.exists())
				itemLocation.mkdirs();
			File itemFile = new File(itemLocation, filename+".item");
			if(!itemFile.exists()) {
				itemFile.createNewFile();
			}
			else
				throw new FileAlreadyExistsException(itemFile.getAbsolutePath());
			FileWriter file = new FileWriter(itemFile);
			file.write("<Type>" + is.getType().name() + "</Type>\n");
			file.append("<Amount>" + is.getAmount() + "</Amount>");
			file.append("<Durability>"+ is.getDurability() + "</Durability>\n");
			if(is.hasItemMeta()) {
				ItemMeta im = is.getItemMeta();
				if (im.hasDisplayName())
					file.append("<DisplayName>" + im.getDisplayName() + "</DisplayName>\n");
				if (im.hasLore()) {
					List<String> loreList = im.getLore();
					file.append("<Lore>\n");
					for(int i = 0; i < loreList.size(); i++)
						file.append(loreList.get(i)+"\n");
					file.append("</Lore>\n");
				}
				if (im.hasEnchants()) {
					file.append("<Enchantments>\n");
					for (Enchantment e:Enchantment.values()) {
						file.append(e.getId()+":"+im.getEnchantLevel(e)+"\n");
					}
					file.append("</Enchantments>");
				}
			}
			file.close();
			return true;
		} catch(FileAlreadyExistsException fe) {
			return false;
		} catch(IOException e) {
			System.err.println("[BookSuite] BookSuiteFileManager.makeFileFromItemStack: "+e);
			e.printStackTrace();
			System.err.println("[BookSuite] End error report.");
			return true;
		}
	}
	
	
	
	
	/**
	 * Parses file text.
	 *
	 * @param text the string to parse
	 * @return the string after parsing
	 */
	public String parseBookText(String text) {
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
		
		text = text.replaceAll("(<|\\[)/(i(talic(s)?)?|b(old)?|u(nderline)?|s(trike)?|del|format|m(agic)?|obf(uscate(d)?)?)(>|\\])", SECTION_SIGN + "r");
		text = text.replaceAll("(<|\\[)/color(>|\\])", SECTION_SIGN + "0");
		text = text.replaceAll("(<|\\[)hr(>|\\])", "\n-------------------\n");
		text = text.replaceAll("(<|\\[)(n|br)(>|\\])", "\n");
		text = text.replaceAll("(" + SECTION_SIGN + "r)+", SECTION_SIGN + "r");
		return text;
	}
	
	
	
	
	/**
	 * Append mail index.
	 *
	 * @param directory the directory
	 * @param appendText the append text
	 * @return true, if successful
	 */
	public boolean appendMailIndex(String directory, String appendText) {
		try {
			File indexLocation = new File(directory);
			if (!indexLocation.exists())
				indexLocation.mkdirs();
			File indexFile = new File(indexLocation, "index.bsm");
			FileWriter index;
			if (indexFile.exists()){
				index = new FileWriter(indexFile);
				index.append(appendText + "\n");
			} else {
				indexFile.createNewFile();
				index = new FileWriter(indexFile);
				index.write(appendText + "\n");
			}
			index.close();
			return true;
		} catch (IOException e) {
			System.err.println("[BookSuite] Error report:\nBookSuiteFileManager.appendMailIndex: " + e);
			e.printStackTrace();
			System.err.println("[BookSuite] End error report.");
			return false;
		}
		
		
	}
	
	
	/**
	 * Removes mail.
	 *
	 * @param directory the directory
	 * @param mail the mail
	 * @return true, if successful
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
				if(!line.equals(mail)) {
					indexContents += line + "\n";
					
				}
			}
			s.close();
			FileWriter writer = new FileWriter(indexFile);
			writer.write(indexContents);
			writer.close();
			delete(directory, mail+".book");
		} catch (FileNotFoundException e) {
			System.err.println("[BookSuite] Error report:\nBookSuiteFileManager.removeMailAndIndex: " + e);
			e.printStackTrace();
			System.err.println("[BookSuite] End error report.");
		} catch (IOException e) {
			System.err.println("[BookSuite] Error report:\nBookSuiteFileManager.appendMailIndex: " + e);
			e.printStackTrace();
			System.err.println("[BookSuite] End error report.");
			return false;
		}
		return true;
	}
	
	
	
	
	/**
	 * Deletes specified file
	 *
	 * @param directory the directory
	 * @param filename the file name
	 * @return true, if successful
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
	 * @param directory the directory
	 * @param p the Player to obtain file list
	 */
	public void listBookFilesIn(String directory, Player p) {
		File file = new File(directory);
		if (!file.exists()) {
			p.sendMessage(ChatColor.DARK_RED + "No books have been saved yet.");
			file.mkdirs();
			return;
		}
		File[] fileList = file.listFiles();//TODO private
		if (fileList == null) {
			p.sendMessage(ChatColor.DARK_RED + "No books found.");
			return;
		}
		String[] bookList = new String[fileList.length];
		int i = 0;
		for (File bookFile : fileList) {
			if (bookFile.getName().contains(".book")) {
				bookList[i] = bookFile.getName().replace(".book", "");
				i++;
			}
		}
		if (bookList.length == 1 && bookList[0].equals("")) {
			p.sendMessage(ChatColor.DARK_RED + "No books found.");
		} else {
			for (String book : bookList) {
				p.sendMessage(ChatColor.DARK_GREEN + book);
			}
			stringyMakinashun(true, true, false, new ArrayList<String>());
		}
	}
	
	
	
	
	public String stringyMakinashun(boolean a, boolean b, boolean c, ArrayList<String> listy) {
		StringBuilder sb = new StringBuilder();
		sb.append("I");
		if (a)
			sb.append(" am ");
		else sb.append(" will ");
		
		if (b && a)
			sb.append("going to eat");
		else if (b) sb.append("be eating");
		else sb.append("eat");
		
		if (c) sb.append(" cheese");
		else sb.append(" delicious chicken");
		
		
		sb.append("! Mmm, good!");
		
		
		listy.add("salad");
		listy.add("hamburgers");
		listy.add("chips");
		
		sb.append("I also plan to eat ");
		
		for (String s : listy) {
			if (listy.indexOf(s) == listy.size() - 1)
				sb.append(" and ");
			sb.append(s + ", ");
			
		}
		
		return sb.substring(0, sb.length() - 2);
	}
	
	
	/** The FileManager instance. */
	private static FileManager instance;
	
	/**
	 * Gets the single instance of FileManager.
	 *
	 * @return single instance of FileManager
	 */
	public static FileManager getInstance() {
		if (instance == null) instance = new FileManager();
		return instance;
	}
}
