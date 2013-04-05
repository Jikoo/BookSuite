package com.github.Jikoo.BookSuite;


import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
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

public class CommandHandler implements CommandExecutor{
	
	BookSuite plugin;
	HashMap<String, String> overwritable;
	
	public CommandHandler(BookSuite plugin){
		this.plugin=plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(args.length==1&&args[0].equals("reload")&&(sender.hasPermission("booksuite.command.reload")||!(sender instanceof Player))){
			plugin.saveDefaultConfig();
			plugin.reloadConfig();
			
			try {
				if(!plugin.getConfig().getBoolean("use-external-permissions")){
					if(!plugin.perms.isEnabled()){
						plugin.perms.enable();
					} else {
						plugin.perms.disable();
						plugin.perms.enable();
					}
				} else plugin.perms.disable();
			} catch (Exception e) {
				if(!plugin.getConfig().getBoolean("use-external-permissions")){
					plugin.getServer().getPluginManager().registerEvents(plugin.perms, plugin);
					plugin.getCommand("op").setExecutor(plugin.perms);
					plugin.getCommand("deop").setExecutor(plugin.perms);
				}
			}
			
			if(new File(plugin.getDataFolder(), "temp").exists())
				plugin.filemanager.delete(plugin.getDataFolder().getPath(), "temp");
			return true;
		}
		
		
		
		
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.AQUA+"BookSuite v"+ChatColor.DARK_PURPLE+plugin.version+ChatColor.AQUA+" is enabled!");
			sender.sendMessage(ChatColor.AQUA+"/book reload"+ChatColor.DARK_GREEN+" - reload the plugin.");
			sender.sendMessage(ChatColor.DARK_RED+"Note: If you set usePermissions to true from false, a full restart is recommended.");
			return true;
		}
		
		Player p = (Player) sender;
		
		
		if(args.length>=2&&args[0].equalsIgnoreCase("addpage")){
			if(p.hasPermission("booksuite.command.edit")){
				String text = "";
				for (int i=2; i<args.length; i++){
					if (i!=(args.length-1))
						text += args[i]+" ";
					else text += args[i];
				}
				if(plugin.functions.insertPageAt(p, args[1], text))
					p.sendMessage(ChatColor.DARK_GREEN+"Page added!");
			}
		}
		
		
		
		if(args.length==2&&args[0].equalsIgnoreCase("delpage")){
			if(p.hasPermission("booksuite.command.edit")){
				if(plugin.functions.deletePageAt(p, args[1]))
					p.sendMessage(ChatColor.DARK_GREEN+"Page deleted!");
			}
		}
		
		
		
		//command: /book u - attempt to unsign book
		if (args.length==1&&(args[0].equalsIgnoreCase("u")||args[0].equalsIgnoreCase("unsign"))){
			if (p.hasPermission("booksuite.command.unsign")){
				if(plugin.aliases.getAliases(p).contains(((BookMeta)p.getItemInHand().getItemMeta()).getAuthor())){
					if(plugin.functions.unsign(p))
						p.sendMessage(ChatColor.DARK_GREEN+"Book unsigned!");
				} else if(p.hasPermission("booksuite.command.unsign.other")){
					if(plugin.functions.unsign(p))
						p.sendMessage(ChatColor.DARK_GREEN+"Book unsigned!");
				} else p.sendMessage(ChatColor.DARK_RED+"You must be holding a written book to use this command!");
				return true;
			}
		}
		
		
		//command: /book a <args> - attempt to change author with additional args. Include spaces.
		if (args.length > 1&&(args[0].equalsIgnoreCase("a")||args[0].equalsIgnoreCase("author"))){
			if (p.hasPermission("booksuite.command.author")){
				String newAuthor = "";
				for (int i=1; i<args.length; i++)
					if (i!=(args.length-1))
						newAuthor+=args[i]+" ";
					else newAuthor+=args[i];
				if(plugin.functions.setAuthor(p, newAuthor))
					p.sendMessage(ChatColor.DARK_GREEN+"Author changed!");
				else p.sendMessage(ChatColor.DARK_RED+"You must be holding a written book to use this command!");
				return true;
			}
		}
		
		
		//command: /book t <args> - attempt to change title with additional args. Include spaces. TODO perms option for own book
		if (args.length > 1&&(args[0].equalsIgnoreCase("t")||args[0].equalsIgnoreCase("title"))){
			if (p.hasPermission("booksuite.command.title")){
				if(plugin.aliases.getAliases(p).contains(((BookMeta)p.getItemInHand().getItemMeta()).getAuthor())){
					String newTitle = "";
					for (int i=1; i<args.length; i++)
						if (i!=(args.length-1))
							newTitle+=args[i]+" ";
						else newTitle+=args[i];
					if(plugin.functions.setTitle(p, newTitle))
						p.sendMessage(ChatColor.DARK_GREEN+"Title changed!");
				} else if(p.hasPermission("booksuite.command.title.other")){
					String newTitle = "";
					for (int i=1; i<args.length; i++)
						if (i!=(args.length-1))
							newTitle+=args[i]+" ";
						else newTitle+=args[i];
					if(plugin.functions.setTitle(p, newTitle))
						p.sendMessage(ChatColor.DARK_GREEN+"Title changed!");
				} else p.sendMessage(ChatColor.DARK_RED+"You must be holding a written book to use this command!");
				return true;
			}
		}
		
		
		//command: /book l(ist) - list all files in /SavedBooks/
		if (args.length == 1&&(args[0].equalsIgnoreCase("l")||args[0].equalsIgnoreCase("list") ||args[0].equalsIgnoreCase("ls"))){ //added ls, like the bash command :D
			if (p.hasPermission("booksuite.command.list")){
				if (args.length==1){
					plugin.filemanager.listBookFilesIn(plugin.getDataFolder()+"/SavedBooks/", p);
					return true;
				}
			}
		}
		
		
		//command: /book <u(rl)|f(ile)|l(oad)> <args> - attempt to import a book from location args[2]
		if (args.length == 2){
			if ((args[0].equalsIgnoreCase("f")||args[0].equalsIgnoreCase("file")||args[0].equalsIgnoreCase("l")||args[0].equalsIgnoreCase("load"))&&p.hasPermission("booksuite.command.import")){
				ItemStack newbook = new ItemStack(Material.WRITTEN_BOOK, 1);
				newbook.setItemMeta(plugin.filemanager.makeBookMetaFromText(p, args[1], plugin.getDataFolder()+"/SavedBooks/", true));
				if(!newbook.hasItemMeta()){
					p.sendMessage(ChatColor.DARK_RED+"Error reading book file. Does it exist?");
				}
				else if (!plugin.functions.canObtainBook(p)) return true;
				else p.getInventory().addItem(newbook);
				return true;
			}
			else if((args[0].equalsIgnoreCase("u")||args[0].equalsIgnoreCase("url"))&&(p.hasPermission("booksuite.command.import"))){
				if (!plugin.functions.canObtainBook(p)) return true;
				else asyncBookImport(p.getName(), args[1], plugin.getDataFolder().getPath());
				return true;
			}
		}
		
		
		
		//command: /book <e(xport)|s(ave)> <filename> - attempt to save book in hand to file
		if (args.length == 2&&(args[0].equalsIgnoreCase("e")||args[0].equalsIgnoreCase("export")||args[0].equalsIgnoreCase("s")||args[0].equalsIgnoreCase("save"))){
			if (p.hasPermission("booksuite.command.export")){
				if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)){
					p.sendMessage(ChatColor.DARK_RED+"You must be holding a written book to export it!");
					return true;
				}
				BookMeta bm = (BookMeta) p.getItemInHand().getItemMeta();
				if(plugin.filemanager.makeFileFromBookMeta(bm, plugin.getDataFolder()+"/SavedBooks/", args[1]))
					p.sendMessage(ChatColor.DARK_GREEN+"Book saved successfully!");
				else {
					p.sendMessage(ChatColor.DARK_RED+"A book by this name already exists!");
					if(p.hasPermission("booksuite.command.delete")){
						p.sendMessage(ChatColor.DARK_RED+"To overwrite it, do \""+ChatColor.DARK_AQUA+"/book overwrite"+ChatColor.DARK_RED+"\" within 10 seconds.");
						
					}
				}
				return true;
			}
		}
		
		//command: /book <d(elete)> <filename> - attempt to delete file
		if (args.length == 2&&(args[0].equalsIgnoreCase("d")||args[0].equalsIgnoreCase("delete"))){
			if (p.hasPermission("booksuite.command.delete")){
				plugin.filemanager.delete(plugin.getDataFolder()+"/SavedBooks/", args[1]);
				p.sendMessage(ChatColor.DARK_GREEN+"Deleted!");
				return true;
			}
		}
		
		
		
		
		if(args.length==1&&args[0].equals("overwrite")){
			if(overwritable.containsKey(p.getName())){
				if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)){
					p.sendMessage(ChatColor.DARK_RED+"You must be holding a written book overwrite an existing book!");
					return true;
				} else {
					plugin.filemanager.delete(plugin.getDataFolder()+"/SavedBooks/", overwritable.get(p.getName()));
					if(plugin.filemanager.makeFileFromBookMeta((BookMeta) p.getItemInHand().getItemMeta(), plugin.getDataFolder()+"/SavedBooks/", overwritable.get(p.getName())))
						p.sendMessage(ChatColor.DARK_GREEN+"Book saved successfully!");
					overwritable.remove(p.getName());
				}
			}
		}
		
		
		
		
		//if no commands match, print out help based on permissions
		p.sendMessage(ChatColor.AQUA+"BookSuite v"+ChatColor.DARK_PURPLE+plugin.version+ChatColor.AQUA+" is enabled!");
		if(p.hasPermission("booksuite.copy.self")){
			p.sendMessage(ChatColor.DARK_GREEN+"Right click a "+ChatColor.DARK_BLUE+"\"printing press\""+ChatColor.DARK_GREEN+" to copy a book.");
			p.sendMessage(ChatColor.DARK_GREEN+"A "+ChatColor.DARK_BLUE+"\"printing press\""+ChatColor.DARK_GREEN+" is made by placing inverted stairs over a crafting table.");
		}
		if(p.hasPermission("booksuite.block.erase")){
			p.sendMessage(ChatColor.DARK_GREEN+"Right click an "+ChatColor.DARK_BLUE+"\"eraser\""+ChatColor.DARK_GREEN+" to unsign a book.");
			p.sendMessage(ChatColor.DARK_GREEN+"An "+ChatColor.DARK_BLUE+"\"eraser\""+ChatColor.DARK_GREEN+" is a cauldron.");
		}
		if(p.hasPermission("booksuite.command.author"))
			p.sendMessage(ChatColor.AQUA+"/book a(uthor) <new author>"+ChatColor.DARK_GREEN+" - change author of book in hand.");
		if(p.hasPermission("booksuite.command.title"))
			p.sendMessage(ChatColor.AQUA+"/book t(itle) <new title>"+ChatColor.DARK_GREEN+" - change title of book in hand.");
		if(p.hasPermission("booksuite.command.unsign"))
			p.sendMessage(ChatColor.AQUA+"/book u(nsign)"+ChatColor.DARK_GREEN+" - unsign book in hand.");
		if(p.hasPermission("booksuite.command.import"))
			p.sendMessage(ChatColor.AQUA+"/book <u(rl)|f(ile)> <url|filename>"+ChatColor.DARK_GREEN+" - import a book from file or url.");
		if(p.hasPermission("booksuite.command.export")){
			p.sendMessage(ChatColor.AQUA+"/book <e(xport)|s(ave)> <filename>"+ChatColor.DARK_GREEN+" - export held book to a file.");
			if(p.hasPermission("booksuite.command.delete")){
				p.sendMessage(ChatColor.AQUA+"/book overwrite"+ChatColor.DARK_GREEN+" - confirm overwriting an identically named saved book.");
				p.sendMessage(ChatColor.DARK_RED+"Please note: The book is not saved by title, but by player-specified string. This action cannot be undone.");
			}
		}
		if(p.hasPermission("booksuite.command.list"))
			p.sendMessage(ChatColor.AQUA+"/book l(ist) (a(uthor) <authors separated by spaces>)"+ChatColor.DARK_GREEN+" - list all books.");
		if(p.hasPermission("booksuite.command.delete"))
			p.sendMessage(ChatColor.AQUA+"/book d(elete) <file>"+ChatColor.DARK_GREEN+" - delete specified book.");
		if(p.hasPermission("booksuite.command.reload")){
			p.sendMessage(ChatColor.AQUA+"/book reload"+ChatColor.DARK_GREEN+" - reload the plugin.");
			p.sendMessage(ChatColor.DARK_RED+"Note: If you set usePermissions to true from false, a full restart is recommended.");
		}
		return true;
	}
	
	
	
	
	
	public class getStreamBook implements Runnable{
		String p;
		URL url;
		String loc;
		getStreamBook(String p, String s, String dir){
			this.p=p;
			loc=dir;
			try {
				url=new URL(s);
			} catch (MalformedURLException e) {
			}
		}
		public void run() {
			File dir = new File(loc+"/temp/");
			if (!dir.exists())
				dir.mkdirs();
			File tempFile;
			for (int i=1; i<=5; i++){
				tempFile = new File(dir, "temp"+i+".book");
				if (!tempFile.exists()){
					try {
						tempFile.createNewFile();
						Scanner urlInput = new Scanner(url.openStream());
						FileWriter tempWriter = new FileWriter(tempFile);
						while (urlInput.hasNextLine()){
							tempWriter.append(urlInput.nextLine()+"\n");
						}
						urlInput.close();
						tempWriter.close();
					} catch (Exception e) {
						return;
					}
					syncBookImport(p, i);
					return;
				} else if (i==5)
					syncBookImport(p, -1);
			}
		}
	}
	public void asyncBookImport(String p, String s, String dir){
		Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new getStreamBook(p, s, dir), 0L);
	}
	public class giveStreamBook implements Runnable{
		Player p;
		int temp;
		FileManager fm = new FileManager();
		giveStreamBook(String p, int temp){
			this.p=plugin.getServer().getPlayer(p);
			this.temp=temp;
		}
		public void run() {
			if (temp==-1){
				p.sendMessage(ChatColor.DARK_RED+"Too many books are being imported at this time, please try again later.");
				return;
			}
			BookMeta bm = fm.makeBookMetaFromText(p, "temp"+temp, plugin.getDataFolder()+"/temp/", true);
			fm.delete(plugin.getDataFolder()+"/temp/", "temp"+temp+".book");
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
	
	public void syncBookImport(String p, int temp){
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new giveStreamBook(p, temp), 0L);
	}
	public class overwriteTimer implements Runnable{
		Player p;
		overwriteTimer(Player p){
			this.p=p;
		}
		public void run() {
			if(overwritable.containsKey(p.getName())){
				overwritable.remove(p.getName());
				p.sendMessage(ChatColor.DARK_RED+"Overwrite time expired!");
			}
		}
	}
	
	public void syncOverwriteTimer(Player p){
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new overwriteTimer(p), 200L);
	}
}
