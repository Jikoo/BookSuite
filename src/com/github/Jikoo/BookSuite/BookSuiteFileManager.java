package com.github.Jikoo.BookSuite;

import java.net.URL;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookSuiteFileManager {
	
	
	public static BookMeta makeBookMetaData(String url, Player p){
		ItemStack newbook = new ItemStack(Material.WRITTEN_BOOK, 1);
		BookMeta text = (BookMeta)newbook.getItemMeta();
		
		text.setAuthor(p.getName());
		
		try {
			URL u = new URL(url);
			Scanner s = new Scanner(u.openStream());
			String page = "";
			while(s.hasNext()){
				String line = s.nextLine();
				
				if (line.length()>=2 && line.substring(0, 2).equals("//")){
					//do nothing, this line is a book comment
				}
				else if (line.contains("<title>")){
					text.setTitle(line.replaceAll("<title>", "").replaceAll("</title>", ""));
				}
				else if(line.contains("<page>")){
					page = "";
				}
				else if (line.contains("</page>")){
					text.addPage(parseText(page));
				}
				else{
					page+=line+"<n>";
				}
			}
			s.close();
			return text;
		}
		catch(Exception ex) {
			p.sendMessage(ChatColor.DARK_RED+"There was a syntax error in the file you provided, or the URL did no check out");
			return null;
		}
	}
	
	
	
	public static String parseText(String text){
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
		text = text.replaceAll("(<|\\[)purple(>|\\])", "§5");
		text = text.replaceAll("(<|\\[)gold(>|\\])", "§6");
		text = text.replaceAll("(<|\\[)grey(>|\\])", "§7");
		text = text.replaceAll("(<|\\[)dark_?grey(>|\\])", "§8");
		text = text.replaceAll("(<|\\[)(indigo|(light_?)?blue)(>|\\])", "§9");
		text = text.replaceAll("(<|\\[)(light_?)?green(>|\\])", "§a");
		text = text.replaceAll("(<|\\[)aqua(>|\\])", "§b");
		text = text.replaceAll("(<|\\[)red(>|\\])", "§c");
		text = text.replaceAll("(<|\\[)pink(>|\\])", "§d");
		text = text.replaceAll("(<|\\[)yellow(>|\\])", "§e");
		text = text.replaceAll("(<|\\[)white(>|\\])", "§f");
		
		text = text.replaceAll("(<|\\[)/(i(talic(s)?)?|b(old)?|u(nderline)?|s(trike)?|del|format)(>|\\])", "§r");
		text = text.replaceAll("(<|\\[)/color(>|\\])", "§0");
		text = text.replaceAll("<n>", "\n");
		text = text.replaceAll("(§r)+", "§r");
		return text;
	}
}
