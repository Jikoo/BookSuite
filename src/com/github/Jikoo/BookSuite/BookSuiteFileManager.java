package com.github.Jikoo.BookSuite;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class BookSuiteFileManager {
	
	private static HashMap<String, String> books = new HashMap<String, String>(); //maps book Title to book Author
	
	private static void writeBookIndexToFile(File f) throws Exception{
		if (! f.exists()) f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(books);
		oos.close();
	}
	
	public static void readBookIndexFromFile(File f) throws Exception{
		ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(f));
		books = (HashMap<String, String>) objIn.readObject();
		objIn.close();
	}
	
	
	public static BookMeta makeBookMetaFromText(Player p, String file, String location, boolean isURL, boolean usePermissions){
		BookMeta text = (BookMeta)new ItemStack(Material.WRITTEN_BOOK, 1).getItemMeta();
		boolean isBookText=false;
		if (!isURL)
			isBookText=true;
		
		try {
			Scanner s;
			if(file.contains(".")) s = new Scanner(new File(location, file));
			else s = new Scanner(new File(location, file+".book"));
			String page = "";
			while(s.hasNext()){
				String line = s.nextLine();
				
				//pastebin support section
				if(location.contains("temp")){
					line=line.replaceAll("(<li class=\").*(\">)", "").replace("</li>", "");
					line=line.replaceAll("(<div class=\").*(\">)", "").replace("</div>", "");
					line=line.replace("&lt;", "<").replace("&gt;", ">");
					line=line.replace("&nbsp", "<n>");
					line=line.replace("Â§", "§");
				}
				
				
				if(line.contains("<book>")){
					isBookText = true;
					line = line.replace("<book>", "");
				}
				if (isBookText){
					if(line.contains("</book>")){
						break;
					}
					if (line.length()>=2 && line.substring(0, 2).equals("//")){
						//do nothing, this line is a book comment
					}
					else if (line.contains("<author>")&&(!isURL||p.hasPermission("booksuite.command.import.other")||(!usePermissions&&p.isOp()))){
						text.setAuthor(line.replace("<author>", "").replace("</author>", ""));
					}
					else if (line.contains("<title>")){
						text.setTitle(line.replace("<title>", "").replace("</title>", "").replace("<br>", ""));
					}
					else if(line.contains("<page>")){
						page = "";
					}
					else if (line.contains("</page>")){
						text.addPage(parseBookText(page));
					}
					else{
						page+=line+"<n>";
					}
				}
			}
			s.close();
			if (!text.hasAuthor())
				text.setAuthor(p.getName());
			return text;
		}
		catch(Exception ex) {
			System.err.println("[BookSuite] Error report:\nBookSuiteFileManager.makeBookMetaFromText: "+ex);
			ex.printStackTrace();
			System.err.println("[BookSuite] End error report.");
			return null;
		}
	}
	
	
	
	
	public static ItemStack makeItemStackFromFile(String directory, String filename){
		ItemStack is = new ItemStack (3, 1);
		ItemMeta im = is.getItemMeta();
		try {
			File itemFile = new File(directory, filename+".item");
			Scanner s = new Scanner(itemFile);
			List<String> lore = new ArrayList<String>();
			boolean handlingEnchants = false;
			while(s.hasNext()){
				String line = s.nextLine();
				
				if (line.contains("<TypeID>")){
					is.setType(Material.matchMaterial(line.replaceAll("<Type>", "").replaceAll("</Type>", "")));
				}
				else if (line.contains("<Amount>")){
					is.setAmount(Integer.parseInt(line.replaceAll("<Amount>", "").replaceAll("</Amount>", "")));
				}
				else if (line.contains("<Durability>")){
					is.setDurability((short)Integer.parseInt(line.replaceAll("<Durability>", "").replaceAll("</Durability>", "")));
				}
				else if(line.contains("<Lore>")){
					lore.clear();
				}
				else if (line.contains("</Lore>")){
					im.setLore(lore);
				}
				else if (line.contains("<DisplayName>")){
					im.setDisplayName(line.replaceAll("<DisplayName>", "").replaceAll("</DisplayName>", ""));
				}
				else if(line.contains("<Enchantments>")){
					handlingEnchants = true;
				}
				else if(line.contains("</Enchantments>")){
					break;
				}
				else if (handlingEnchants){
					String[] enchant = line.split(":");
					im.addEnchant(Enchantment.getById(Integer.parseInt(enchant[0])), Integer.parseInt(enchant[1]), true);
				}
				else{
					lore.add(line);
				}
			}
			s.close();
			is.setItemMeta(im);
			return is;
		}
		catch(Exception ex) {
			im.setDisplayName("Item file error! My condolences.");
			is.setItemMeta(im);
			return is;
		}
	}
	
	
	
	
	
	public static boolean makeFileFromBookMeta(BookMeta bm, String directory, String filename){
		
		try {
			File bookLocation = new File(directory);
			if (!bookLocation.exists())
				bookLocation.mkdirs();
			File bookFile = new File(bookLocation, filename+".book");
			if(!bookFile.exists()){
				bookFile.createNewFile();
			}
			else
				throw new FileAlreadyExistsException(bookFile.getAbsolutePath());
			FileWriter file = new FileWriter(bookFile);
			file.write("<book>\n");
			file.append("<author>"+bm.getAuthor()+"</author>\n");
			file.append("<title>"+bm.getTitle()+"</title>\n");
			for (int i=1; i<=bm.getPageCount(); i++)
				file.append("<page>\n"+bm.getPage(i)+"\n</page>\n");
			file.append("</book>");
			file.close();
			
			
			books.put(bm.getTitle(), bm.getAuthor());
			writeBookIndexToFile(new File(bookLocation, "books.index"));
			return true;
		}
		catch(FileAlreadyExistsException fe){
			return false;
		}
		catch(Exception e) {
			System.err.println("[BookSuite] BookSuiteFileManager.makeFileFromBookMeta: "+e);
			e.printStackTrace();
			System.err.println("[BookSuite] End error report.");
			return true;
		}
	}
	
	
	
	
	public static boolean makeFileFromItemStack(ItemStack is, String directory, String filename){
		try {
			File itemLocation = new File(directory);
			if (!itemLocation.exists())
				itemLocation.mkdirs();
			File itemFile = new File(itemLocation, filename+".item");
			if(!itemFile.exists()){
				itemFile.createNewFile();
			}
			else
				throw new FileAlreadyExistsException(itemFile.getAbsolutePath());
			FileWriter file = new FileWriter(itemFile);
			file.write("<Type>"+is.getType().name()+"</Type>\n");
			file.append("<Amount>"+is.getAmount()+"</Amount>");
			file.append("<Durability>"+is.getDurability()+"</Durability>\n");
			if(is.hasItemMeta()){
				ItemMeta im = is.getItemMeta();
				if (im.hasDisplayName())
					file.append("<DisplayName>"+im.getDisplayName()+"</DisplayName>\n");
				if (im.hasLore()){
					List<String> loreList = im.getLore();
					file.append("<Lore>\n");
					for(int i=0; i< loreList.size(); i++)
						file.append(loreList.get(i)+"\n");
					file.append("</Lore>\n");
				}
				if (im.hasEnchants()){
					file.append("<Enchantments>\n");
					for (Enchantment e:Enchantment.values()){
						file.append(e.getId()+":"+im.getEnchantLevel(e)+"\n");
					}
					file.append("</Enchantments>");
				}
			}
			file.close();
			return true;
		}
		catch(FileAlreadyExistsException fe){
			return false;
		}
		catch(IOException e) {
			System.err.println("[BookSuite] BookSuiteFileManager.makeFileFromItemStack: "+e);
			e.printStackTrace();
			System.err.println("[BookSuite] End error report.");
			return true;
		}
	}
	
	
	
	
	public static String parseBookText(String text){
		text = text.replaceAll("(<|\\[)i(talic(s)?)?(>|\\])", "§o");
		text = text.replaceAll("(<|\\[)b(old)?(>|\\])", "§l");
		text = text.replaceAll("(<|\\[)u(nderline)?(>|\\])", "§n");
		text = text.replaceAll("(<|\\[)(s(trike)?|del)(>|\\])", "§m");
		text = text.replaceAll("(<|\\[)(m(agic)?|obf(uscate(d)?)?)(>|\\])", "§k");
		
		text = text.replaceAll("(<|\\[)color=", "<");
 		text = text.replaceAll("(<|\\[)black(>|\\])", "§0");
		text = text.replaceAll("(<|\\[)dark_?blue(>|\\])", "§1");
		text = text.replaceAll("(<|\\[)dark_?green(>|\\])", "§2");
		text = text.replaceAll("(<|\\[)dark_?aqua(>|\\])", "§3");
		text = text.replaceAll("(<|\\[)dark_?red(>|\\])", "§4");
		text = text.replaceAll("(<|\\[)(purple|magenta)(>|\\])", "§5");
		text = text.replaceAll("(<|\\[)gold(>|\\])", "§6");
		text = text.replaceAll("(<|\\[)gr[ea]y(>|\\])", "§7");
		text = text.replaceAll("(<|\\[)dark_?gr[ea]y(>|\\])", "§8");
		text = text.replaceAll("(<|\\[)(indigo|(light_?)?blue)(>|\\])", "§9");
		text = text.replaceAll("(<|\\[)(light_?|bright_?)?green(>|\\])", "§a");
		text = text.replaceAll("(<|\\[)aqua(>|\\])", "§b");
		text = text.replaceAll("(<|\\[)(light_?)?red(>|\\])", "§c");
		text = text.replaceAll("(<|\\[)pink(>|\\])", "§d");
		text = text.replaceAll("(<|\\[)yellow(>|\\])", "§e");
		text = text.replaceAll("(<|\\[)white(>|\\])", "§f");
		
		text = text.replaceAll("(<|\\[)/(i(talic(s)?)?|b(old)?|u(nderline)?|s(trike)?|del|format|m(agic)?|obf(uscate(d)?)?)(>|\\])", "§r");
		text = text.replaceAll("(<|\\[)/color(>|\\])", "§0");
		text = text.replaceAll("(<|\\[)hr(>|\\])", "\n-------------------\n");
		text = text.replaceAll("(<|\\[)(n|br)(>|\\])", "\n");
		text = text.replaceAll("(§r)+", "§r");
		return text;
	}
	
	
	
	
	public static boolean appendMailIndex(String directory, String appendText){
		try {
			File indexLocation = new File(directory);
			if (!indexLocation.exists())
				indexLocation.mkdirs();
			File indexFile = new File(indexLocation, "index.bsm");
			FileWriter index;
			if (indexFile.exists()){
				index = new FileWriter(indexFile);
				index.append(appendText+"\n");
			}
			else {
				indexFile.createNewFile();
				index = new FileWriter(indexFile);
				index.write(appendText+"\n");
			}
			index.close();
			return true;
		} catch (IOException e) {
			System.err.println("[BookSuite] Error report:\nBookSuiteFileManager.appendMailIndex: "+e);
			e.printStackTrace();
			System.err.println("[BookSuite] End error report.");
			return false;
		}
		
		
	}
	
	
	public static boolean removeMail(String directory, String mail){
		try {
			File indexFile = new File(directory, "index.bsm");
			if (!indexFile.exists())
				return false;
			Scanner s = new Scanner(indexFile);
			String indexContents="";
			
			while (s.hasNextLine()){
				String line = s.nextLine();
				if(!line.equals(mail)){
					indexContents+=line+"\n";
					
				}
			}
			s.close();
			FileWriter writer = new FileWriter(indexFile);
			writer.write(indexContents);
			writer.close();
			delete(directory, mail+".book");
		} catch (FileNotFoundException e) {
			System.err.println("[BookSuite] Error report:\nBookSuiteFileManager.removeMailAndIndex: "+e);
			e.printStackTrace();
			System.err.println("[BookSuite] End error report.");
		} catch (IOException e) {
			System.err.println("[BookSuite] Error report:\nBookSuiteFileManager.appendMailIndex: "+e);
			e.printStackTrace();
			System.err.println("[BookSuite] End error report.");
		return false;
	}
		
		
		
		return true;
	}
	
	
	
	
	public static void delete(String directory, String filename){
		File file = new File(directory, filename);
		if (!file.exists())
			return;
		file.delete();
	}
	
	public static void listBookFilesIn(String directory, Player p){
		File file = new File(directory);
		if (!file.exists()){
			p.sendMessage(ChatColor.DARK_RED+"No books have been saved yet.");
			file.mkdirs();
			return;
		}
		File[] fileList = file.listFiles();
		if (fileList==null){
			p.sendMessage(ChatColor.DARK_RED+"No books found.");
			return;
		}
		String[] bookList = new String[fileList.length];
		int i = 0;
		for (File bookFile : fileList){
			if (bookFile.getName().contains(".book")){
				bookList[i] = bookFile.getName().replace(".book", "").replace(".txt", "");
				i++;
			}
		}
		if (bookList.length==1&&bookList[0].equals("")){
			p.sendMessage(ChatColor.DARK_RED+"No books found.");
		} else {
			for (String book : bookList){
				p.sendMessage(ChatColor.DARK_GREEN+book);
			}
		}
	}
	
	public static void listBookFilesByAuthor(String directory, Player p, String[] authors) throws Exception{
		File file = new File(directory);
		BookSuiteFileManager.readBookIndexFromFile(new File(file, "book.index"));
		
		
		if (!file.exists()){
			p.sendMessage(ChatColor.DARK_RED+"No books have been saved yet.");
			file.mkdirs();
			return;
		}
		File[] fileList = file.listFiles();
		if (fileList==null){
			p.sendMessage(ChatColor.DARK_RED+"No books found.");
			return;
		}
		String[] bookList = new String[fileList.length];
		int i = 0;
		for (File bookFile : fileList){
			if (bookFile.getName().contains(".book")){
				bookList[i] = bookFile.getName().replace(".book", "").replace(".txt", "");
				i++;
			}
		}
		
		
		for(String a : authors){
			String book = a+": ";
			for(String s : bookList){
				book+=s+",";
			}
			book.substring(0, book.length()-2);
			p.sendMessage(ChatColor.DARK_GREEN+book);
		}
			
	}
}
