package com.github.Jikoo.BookSuite;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookSuiteCommandExecutor implements CommandExecutor{
	
	BookSuite plugin;
	
	public BookSuiteCommandExecutor(BookSuite plugin){
		this.plugin=plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.DARK_RED+"No commands in BookSuite v"+plugin.version+" can be run from the console.");
			return false;
		}
		
		Player p = (Player) sender;
		
		
		//command: /book e - try to give them a sponge
		if (args.length==1&&(args[0].equalsIgnoreCase("e")||args[0].equalsIgnoreCase("eraser"))){
			if (p.hasPermission("booksuite.command.eraser")||!plugin.usePermissions){
				if(p.getInventory().firstEmpty()!=-1)
					if (!p.getInventory().contains(Material.SPONGE)){
						p.getInventory().addItem(new ItemStack(Material.SPONGE, 1));
						p.updateInventory();
					}
					else p.sendMessage(ChatColor.DARK_RED+"You already have an eraser!");
				else p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
				return true;
			}
			else p.sendMessage(ChatColor.DARK_RED+"You do not have permission to use that command!");
			return false;
		}
		
		
		//command: /book u - attempt to unsign book
		if (args.length==1&&(args[0].equalsIgnoreCase("u")||args[0].equalsIgnoreCase("unsign"))){
			if (p.hasPermission("booksuite.command.unsign")||(!plugin.usePermissions&&p.isOp())){
				if(BookSuiteFunctions.unsign(p))
					p.sendMessage(ChatColor.DARK_GREEN+"Book unsigned!");
				else p.sendMessage(ChatColor.DARK_RED+"You must be holding a written book to use this command!");
				return true;
			}
			else p.sendMessage(ChatColor.DARK_RED+"You do not have permission to use that command!");
			return false;
		}
		
		
		//command: /book a <args> - attempt to change author with additional args. Include spaces.
		if (args.length > 1&&(args[0].equalsIgnoreCase("a")||args[0].equalsIgnoreCase("author"))){
			if (p.hasPermission("booksuite.command.author")||(!plugin.usePermissions&&p.isOp())){
				String newAuthor = "";
				for (int i=1; i<args.length; i++)
					if (i!=(args.length-1))
						newAuthor+=args[i]+" ";
					else newAuthor+=args[i];
				if(BookSuiteFunctions.setAuthor(p, newAuthor))
					p.sendMessage(ChatColor.DARK_GREEN+"Author changed!");
				else p.sendMessage(ChatColor.DARK_RED+"You must be holding a written book to use this command!");
				return true;
			}
			else p.sendMessage(ChatColor.DARK_RED+"You do not have permission to use that command!");
			return false;
		}
		
		
		//command: /book t <args> - attempt to change title with additional args. Include spaces.
		if (args.length > 1&&(args[0].equalsIgnoreCase("t")||args[0].equalsIgnoreCase("title"))){
			if (p.hasPermission("booksuite.command.title")||(!plugin.usePermissions&&p.isOp())){
				String newTitle = "";
				for (int i=1; i<args.length; i++)
					if (i!=(args.length-1))
						newTitle+=args[i]+" ";
					else newTitle+=args[i];
				if(BookSuiteFunctions.setTitle(p, newTitle))
					p.sendMessage(ChatColor.DARK_GREEN+"Title changed!");
				else p.sendMessage(ChatColor.DARK_RED+"You must be holding a written book to use this command!");
				return true;
			}
			else p.sendMessage(ChatColor.DARK_RED+"You do not have permission to use that command!");
			return false;
		}
		
		
		//command: /book l(ist) - list all files in /SavedBooks/
		if (args.length >= 1&&(args[0].equalsIgnoreCase("l")||args[0].equalsIgnoreCase("list") ||args[0].equalsIgnoreCase("ls"))){ //added ls, like the bash command :D
			if (p.hasPermission("booksuite.command.list")||!plugin.usePermissions){
				if (args.length==1)BookSuiteFileManager.listBookFilesIn(plugin.getDataFolder()+"/SavedBooks/", p);
				if (args.length>2){
					if (args[1].equalsIgnoreCase("a") || args[1].equalsIgnoreCase("author")){
						String[] authors = new String[args.length-2];
						for(int i = 2; i < args.length; i++){
							authors[i-2] = args[i]; 
						}
						try {
							BookSuiteFileManager.listBookFilesByAuthor(plugin.getDataFolder()+"/SavedBooks/", p, authors);
						} catch (Exception e) {
							
						}
					}
				}
			}
			else p.sendMessage(ChatColor.DARK_RED+"You do not have permission to use that command!");
			return false;
		}
		
		
		//command: /book <u(rl)|f(ile)|l(oad)> <args> - attempt to import a book from location args[2] - only possible command conflict is if book
		if (args.length == 2){
			boolean validImport = false;
			boolean isURL=false;
			if (args[0].equalsIgnoreCase("f")||args[0].equalsIgnoreCase("file")||args[0].equalsIgnoreCase("l")||args[0].equalsIgnoreCase("load"))
				validImport=true;
			else if(args[0].equalsIgnoreCase("u")||args[0].equalsIgnoreCase("url")){
				isURL=true;
			}
			if (isURL && (p.hasPermission("booksuite.command.import")||!plugin.usePermissions)){
				if (!BookSuiteFunctions.canObtainBook(p, plugin.usePermissions)) return true;
				else asyncBookImport(p, args [1]);
			}
			else if (validImport && (p.hasPermission("booksuite.command.import")||!plugin.usePermissions)){
				ItemStack newbook = new ItemStack(Material.WRITTEN_BOOK, 1);
				newbook.setItemMeta(BookSuiteFileManager.makeBookMetaFromText(p, args[1], plugin.getDataFolder()+"/SavedBooks/", !isURL, plugin.usePermissions));
				if(!newbook.hasItemMeta()){
					p.sendMessage(ChatColor.DARK_RED+"Error reading book file. Does it exist?");
				}
				else if (!BookSuiteFunctions.canObtainBook(p, plugin.usePermissions)) return true;
				else p.getInventory().addItem(newbook);
				return true;
			}
		}
		
		
		
		//command: /book <e(xport)|s(ave)> <filename> - attempt to save book in hand to file
		if (args.length == 2&&(args[0].equalsIgnoreCase("e")||args[0].equalsIgnoreCase("export")||args[0].equalsIgnoreCase("s")||args[0].equalsIgnoreCase("save"))){
			if (p.hasPermission("booksuite.command.export")||!plugin.usePermissions){
				if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)){
					p.sendMessage(ChatColor.DARK_RED+"You must be holding a written book to export it!");
					return true;
				}
				BookMeta bm = (BookMeta) p.getItemInHand().getItemMeta();
				if(BookSuiteFileManager.makeFileFromBookMeta(bm, plugin.getDataFolder()+"/SavedBooks/", args[1]))
					p.sendMessage(ChatColor.DARK_GREEN+"Book saved successfully!");
				else p.sendMessage(ChatColor.DARK_RED+"A book by this name already exists!");
				return true;
			}
		}
		
		//command: /book <d(elete)> <filename> - attempt to delete file
		if (args.length == 2&&(args[0].equalsIgnoreCase("d")||args[0].equalsIgnoreCase("delete"))){
			if (p.hasPermission("booksuite.command.delete")||(!plugin.usePermissions&&p.isOp())){
				BookSuiteFileManager.delete(plugin.getDataFolder()+"/SavedBooks/", args[1]);
				p.sendMessage(ChatColor.DARK_GREEN+"Deleted!");
				return true;
			}
		}
		
		
		
		
		//if no commands match, print out help based on permissions
		p.sendMessage(ChatColor.AQUA+"BookSuite v"+ChatColor.DARK_PURPLE+plugin.version+ChatColor.AQUA+" enabled!");
		if(p.hasPermission("booksuite.copy.self")||!plugin.usePermissions){
			p.sendMessage(ChatColor.DARK_GREEN+"Right click a "+ChatColor.DARK_BLUE+"\"printing press\""+ChatColor.DARK_GREEN+" to copy a book.");
			p.sendMessage(ChatColor.DARK_GREEN+"A "+ChatColor.DARK_BLUE+"\"printing press\""+ChatColor.DARK_GREEN+" is made by placing inverted stairs over a crafting table.");
		}
		if(p.hasPermission("booksuite.block.erase")||!plugin.usePermissions){
			p.sendMessage(ChatColor.DARK_GREEN+"Right click an \"eraser\" to unsign a book.");
			p.sendMessage(ChatColor.DARK_GREEN+"An "+ChatColor.DARK_BLUE+"\"eraser\""+ChatColor.DARK_GREEN+" is a sponge.");
			if(p.hasPermission("booksuite.command.eraser")||!plugin.usePermissions)
				p.sendMessage(ChatColor.AQUA+"/book e(raser)"+ChatColor.DARK_GREEN+" - get an eraser.");
		}
		if(p.hasPermission("booksuite.command.author")||(!plugin.usePermissions&&p.isOp()))
			p.sendMessage(ChatColor.AQUA+"/book a(uthor) <new author>"+ChatColor.DARK_GREEN+" - change author of book in hand.");
		if(p.hasPermission("booksuite.command.title")||(!plugin.usePermissions&&p.isOp()))
			p.sendMessage(ChatColor.AQUA+"/book t(itle) <new title>"+ChatColor.DARK_GREEN+" - change title of book in hand.");
		if(p.hasPermission("booksuite.command.unsign")||(!plugin.usePermissions&&p.isOp()))
			p.sendMessage(ChatColor.AQUA+"/book u(nsign)"+ChatColor.DARK_GREEN+" - unsign book in hand.");
		if(p.hasPermission("booksuite.command.import")||!plugin.usePermissions)
			p.sendMessage(ChatColor.AQUA+"/book <u(rl)|f(ile)> <url|filename>"+ChatColor.DARK_GREEN+" - import a book from file or url.");
		if(p.hasPermission("booksuite.command.export")||!plugin.usePermissions)
			p.sendMessage(ChatColor.AQUA+"/book <e(xport)|s(ave)> <filename>"+ChatColor.DARK_GREEN+" - export held book to a file.");
		if(p.hasPermission("booksuite.command.list")||!plugin.usePermissions)
			p.sendMessage(ChatColor.AQUA+"/book l(ist) (a(uthor) <authors separated by spaces>)"+ChatColor.DARK_GREEN+" - list all books or books by specified author(s).");
		if(p.hasPermission("booksuite.command.delete")||(!plugin.usePermissions&&p.isOp()))
			p.sendMessage(ChatColor.AQUA+"/book d(elete) <file>"+ChatColor.DARK_GREEN+" - delete specified book.");
		return true;
	}
	
	
	
	
	
	public class getStreamBook implements Runnable{
		Player p;
		URL url;
		getStreamBook(Player p, String s){
			this.p=p;
			try {
				url=new URL(s);
			} catch (MalformedURLException e) {
			}
		}
		public void run() {
			File dir = new File(plugin.getDataFolder()+"/temp/");
			if (!dir.exists())
				dir.mkdirs();
			for (int i=1; i<=5; i++){
				if (!new File(dir, "temp"+i).exists()){
					File tempFile = new File(dir, "temp"+i);
					try {
						Scanner urlInput = new Scanner(url.openStream());
						FileWriter tempWriter = new FileWriter(tempFile);
						while (urlInput.hasNextLine()){
							tempWriter.append(urlInput.nextLine());
						}
						urlInput.close();
						tempWriter.close();
					} catch (IOException e) {
						return;
					}
					syncBookImport(p, i);
					return;
				} else if (i==5)
					syncBookImport(p, -1);
			}
		}
	}
	public void asyncBookImport(Player p, String s){
		Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new getStreamBook(p, s), 0L);
	}
	public class giveStreamBook implements Runnable{
		Player p;
		int temp;
		giveStreamBook(Player p, int temp){
			this.p=p;
			this.temp=temp;
		}
		public void run() {
			if (temp==-1){
				p.sendMessage(ChatColor.DARK_RED+"Too many books are being imported at this time, please try again later.");
				return;
			}
			BookMeta bm = BookSuiteFileManager.makeBookMetaFromText(p, "temp"+temp, plugin.getDataFolder()+"/temp/", true, plugin.usePermissions);
			BookSuiteFileManager.delete(plugin.getDataFolder()+"/temp/", "temp"+temp);
			if (bm!=null){
				ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
				is.setItemMeta(bm);
				if(p.getInventory().firstEmpty()!=-1){
					p.getInventory().addItem(is);
				} else {
					p.getWorld().dropItem(p.getLocation(), is);
				}
			}
			else {
				p.sendMessage(ChatColor.DARK_RED+"Error reading from URL.");
				if(p.getInventory().firstEmpty()>0){
					p.getInventory().addItem(new ItemStack(Material.INK_SACK, 1));
					p.getInventory().addItem(new ItemStack(Material.BOOK, 1));
				} else {
					p.sendMessage(ChatColor.DARK_RED+"Dropped book supplies at your feet.");
					p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.INK_SACK, 1));
					p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.BOOK, 1));
				}
			}
		}
	}
	
	public void syncBookImport(Player p, int temp){
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new giveStreamBook(p, temp), 0L);
	}
	
}
