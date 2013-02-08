package com.github.Jikoo.BookSuite;

import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class BookSuiteFileManager {
	
	
	public static BookMeta makeBookMetaFromText(String file, String location, String type){
		BookMeta text = (BookMeta)new ItemStack(Material.WRITTEN_BOOK, 1).getItemMeta();
		
		
		try {
			Scanner s;
			if (type.equalsIgnoreCase("url")||type.equalsIgnoreCase("u")){
				URL u = new URL(file);
				s = new Scanner(u.openStream());
			} else s = new Scanner(new File(location, file));
			String page = "";
			while(s.hasNext()){
				String line = s.nextLine();
				
				if (line.length()>=2 && line.substring(0, 2).equals("//")){
					//do nothing, this line is a book comment
				}
				else if (line.contains("<author>")){
					text.setAuthor(line.replaceAll("<author>", "").replaceAll("</author>", ""));
				}
				else if (line.contains("<title>")){
					text.setTitle(line.replaceAll("<title>", "").replaceAll("</title>", ""));
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
			s.close();
			return text;
		}
		catch(Exception ex) {
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
			File bookFile = new File(directory, filename);
			FileWriter file = new FileWriter(bookFile);
			file.write("<author>"+bm.getAuthor()+"</author>\n");
			file.append("<title>"+bm.getTitle()+"</title>\n");
			for (int i=1; i<=bm.getPageCount(); i++)
				file.append("<page>\n"+bm.getPage(i)+"\n</page>\n");
			file.close();
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	
	
	
	public static boolean makeFileFromItemStack(ItemStack is, String directory, String filename){
		try {
			File bookFile = new File(directory, filename+".item");
			FileWriter file = new FileWriter(bookFile);
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
		catch(Exception e) {
			return false;
		}
	}
	
	
	
	
	public static String parseBookText(String text){
		text = text.replaceAll("(<|\\[)i(talic(s)?)?(>|\\])", "§o");
		text = text.replaceAll("(<|\\[)b(old)?(>|\\])", "§l");
		text = text.replaceAll("(<|\\[)u(nderline)?(>|\\])", "§n");
		text = text.replaceAll("(<|\\[)(s(trike)?|del)(>|\\])", "§m");
		text = text.replaceAll("(<|\\[)m(agic)?(>|\\])", "§k");
		
		text = text.replaceAll("(<|\\[)color=", "<");
 		text = text.replaceAll("(<|\\[)black(>|\\])", "§0");
		text = text.replaceAll("(<|\\[)dark_?blue(>|\\])", "§1");
		text = text.replaceAll("(<|\\[)dark_?green(>|\\])", "§2");
		text = text.replaceAll("(<|\\[)dark_?aqua(>|\\])", "§3");
		text = text.replaceAll("(<|\\[)dark_?red(>|\\])", "§4");
		text = text.replaceAll("(<|\\[)(purple|magenta)(>|\\])", "§5");
		text = text.replaceAll("(<|\\[)gold(>|\\])", "§6");
		text = text.replaceAll("(<|\\[)gr(e|a)y(>|\\])", "§7");
		text = text.replaceAll("(<|\\[)dark_?grey(>|\\])", "§8");
		text = text.replaceAll("(<|\\[)(indigo|(light_?)?blue)(>|\\])", "§9");
		text = text.replaceAll("(<|\\[)(light_?|bright_?)?green(>|\\])", "§a");
		text = text.replaceAll("(<|\\[)aqua(>|\\])", "§b");
		text = text.replaceAll("(<|\\[)(light_?)?red(>|\\])", "§c");
		text = text.replaceAll("(<|\\[)pink(>|\\])", "§d");
		text = text.replaceAll("(<|\\[)yellow(>|\\])", "§e");
		text = text.replaceAll("(<|\\[)white(>|\\])", "§f");
		
		text = text.replaceAll("(<|\\[)/(i(talic(s)?)?|b(old)?|u(nderline)?|s(trike)?|del|format)(>|\\])", "§r");
		text = text.replaceAll("(<|\\[)/color(>|\\])", "§0");
		text = text.replaceAll("(<|\\[)hr(>|\\])", "\n-------------------\n");
		text = text.replaceAll("(<|\\[)(n|br)(>|\\])", "\n");
		text = text.replaceAll("(§r)+", "§r");
		return text;
	}
	
	
	
	
	public static boolean appendMailIndex(String directory, String appendText){
		try {
			File indexFile = new File(directory, "index.bsm");
			FileWriter index = new FileWriter(indexFile);;
			if (indexFile.exists()) index.append(appendText+"\n");
			else {
				if(indexFile.mkdirs())
					index.write(appendText+"\n");
				else {
					index.close();
					return false;
				}
			}
			index.close();
			return true;
		} catch (Exception e) {
			return false;
		}
		
		
	}
	
	
	
	
	public static void delete(String directory, String filename){
		File file = new File(directory, filename);
		if (!file.exists())
			return;
		file.delete();
	}
	
	
}
