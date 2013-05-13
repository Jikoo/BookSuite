package com.github.Jikoo.BookSuite;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

import com.github.Jikoo.BookSuite.metrics.Metrics;
import com.github.Jikoo.BookSuite.permissions.PermissionsListener;
import com.github.Jikoo.BookSuite.rules.Rules;
import com.github.Jikoo.BookSuite.update.UpdateCheck;

public class CommandHandler implements CommandExecutor {
	
	BookSuite plugin = BookSuite.getInstance();
	HashMap<String, String> overwritable = new HashMap<String, String>();
	
	private static CommandHandler instance;
	public static CommandHandler getInstance() {
		if (instance == null) instance = new CommandHandler();
		return instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {//TODO completely reorganize commands for efficiency
		if (args.length == 1 && args[0].equalsIgnoreCase("reload") && (sender.hasPermission("booksuite.command.reload") || !(sender instanceof Player))) {
			plugin.saveDefaultConfig();
			plugin.reloadConfig();
			
			
			if (plugin.getConfig().getBoolean("use-inbuilt-permissions")) {
				if (plugin.perms != null) {
					if (!plugin.perms.isEnabled()) {
						plugin.perms.enable();
					} else {
						plugin.perms.disable();
						plugin.perms.enable();
					}
				} else {
					plugin.perms = new PermissionsListener(plugin);
					plugin.perms.enable();
				}
			} else {
				if (plugin.perms != null) {
					plugin.perms.disable();
					plugin.perms = null;
				}
			}
			
			
			
			try {
				if (plugin.getConfig().getBoolean("enable-metrics")) {
					if (plugin.metrics == null) {
						try {
							plugin.metrics = new Metrics(plugin);
							plugin.metrics.start();
						} catch (IOException e) {
							plugin.getLogger().warning("[BookSuite] Error enabling metrics: " + e);
							e.printStackTrace();
							plugin.getLogger().warning("[BookSuite] End error report.");
							if (plugin.metrics != null) {
								plugin.metrics.disable();
								plugin.metrics = null;
							}
						}
					} else {
						plugin.metrics.start();
					}
				} else {
					if (plugin.metrics != null) {
						plugin.metrics.disable();
						plugin.metrics = null;
					}
				}
			} catch (Exception e) {
				plugin.getLogger().warning("[BookSuite] Error reloading metrics: " + e);
				e.printStackTrace();
				plugin.getLogger().warning("[BookSuite] End error report.");
			}
			
			
			
			if (plugin.getConfig().getBoolean("login-update-check")) {
				if (plugin.update == null)
					plugin.update = new UpdateCheck(plugin);
				plugin.update.disableNotifications();
				plugin.update.enableNotifications();
			} else {
				if (plugin.update != null)
					plugin.update.disableNotifications();
			}
			
			
			
			plugin.alias.load();
			
			
			
			if (plugin.getConfig().getBoolean("book-rules")) {
				if (plugin.rules == null) {
					plugin.rules = new Rules();
					plugin.rules.enable();
				} else plugin.rules.load();
			} else if (plugin.rules != null) {
				plugin.rules.disable();
				plugin.rules = null;
			}
			
			
			if (new File(plugin.getDataFolder(), "temp").exists())
				plugin.filemanager.delete(plugin.getDataFolder().getPath(), "temp");
			sender.sendMessage(ChatColor.DARK_GREEN + "BookSuite v" + ChatColor.DARK_PURPLE + plugin.version + ChatColor.AQUA + " reloaded!");
			return true;
		}
		
		if (args.length == 1 && args[0].equalsIgnoreCase("update") && (sender.hasPermission("booksuite.command.update") || !(sender instanceof Player))) {
			plugin.update.delayUpdateCheck(sender, true, 0L);
			return true;
		}
		
		
		
		
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.AQUA + "BookSuite v"+ChatColor.DARK_PURPLE + plugin.version + ChatColor.AQUA + " is enabled!");
			sender.sendMessage(ChatColor.AQUA + "book reload"+ChatColor.DARK_GREEN + " - reload the plugin.");
			sender.sendMessage(ChatColor.AQUA + "book update"+ChatColor.DARK_GREEN + " - check for plugin updates.");
			return true;
		}
		
		Player p = (Player) sender;
		
		
		
		
		if (args.length >= 2 && args[0].equalsIgnoreCase("addpage")) {
			if (p.hasPermission("booksuite.command.edit")) {
				String text = "";
				for (int i = 2; i < args.length; i++) {
					if (i != (args.length - 1))
						text += args[i] + " ";
					else text += args[i];
				}
				if (plugin.functions.insertPageAt(p, args[1], text))
					p.sendMessage(ChatColor.DARK_GREEN + "Page added!");
				return true;
			}
		}
		
		
		
		
		if (args.length == 2 && args[0].equalsIgnoreCase("delpage")) {
			if (p.hasPermission("booksuite.command.edit")) {
				if (plugin.functions.deletePageAt(p, args[1]))
					p.sendMessage(ChatColor.DARK_GREEN + "Page deleted!");
				return true;
			}
		}
		
		
		
		
		if (args.length >= 1 && args[0].equalsIgnoreCase("copy")) {//TODO more efficient booksuite.copy.free
			if (p.hasPermission("booksuite.command.copy")) {
				int copies;
				if (args.length >= 2) {
					try {
						copies = Integer.parseInt(args[1]);
					} catch (NumberFormatException e) {
						p.sendMessage(ChatColor.DARK_RED + args[1] + " is not a valid integer. Assuming 1..");
						copies = 1;
					}
				} else copies = 1;
				ItemStack is = p.getItemInHand();
				if (is.getType().equals(Material.MAP)) {
					for (int i = 0; i < copies; i++) {
						if (plugin.functions.canObtainMap(p))
							plugin.functions.copy(p);
						else break;
					}
					return true;
				} else if (!(is.hasItemMeta() || is.getItemMeta() != null)) {
					p.sendMessage(ChatColor.DARK_RED + "There doesn't seem to be any writing to copy.");
				} else if (is.getType().equals(Material.WRITTEN_BOOK)) {
					BookMeta bm = (BookMeta) is.getItemMeta();
					if (plugin.functions.checkCommandCopyPermission(p, bm.getAuthor())) {
						for (int i = 0; i < copies; i++) {
							if (plugin.functions.canObtainBook(p))
								plugin.functions.copy(p);
							else break;
						}
					}
					return true;
				} else if (is.getType().equals(Material.BOOK_AND_QUILL)) {
					if (p.hasPermission("booksuite.copy.unsigned")) {
						for (int i = 0; i < copies; i++) {
							if (plugin.functions.canObtainBook(p))
								plugin.functions.copy(p);
							else break;
						}
					} else p.sendMessage(ChatColor.DARK_RED + "You do not have permission to copy unsigned books!");
					return true;
				} else p.sendMessage(ChatColor.DARK_RED + "You must be holding a copiable item to use this command!");
				return true;
			}
		}
		
		
		
		
		//command: /book u - attempt to unsign book
		if (args.length == 1 && (args[0].equalsIgnoreCase("u") || args[0].equalsIgnoreCase("unsign"))) {
			if (p.hasPermission("booksuite.command.unsign")) {
				if (p.getName().equals(((BookMeta) p.getItemInHand().getItemMeta()).getAuthor())) {
					if (plugin.functions.unsign(p))
						p.sendMessage(ChatColor.DARK_GREEN + "Book unsigned!");
					else p.sendMessage(ChatColor.DARK_RED + "You must be holding a written book to use this command!");
				} else if (p.hasPermission("booksuite.command.unsign.other")) {
					if (plugin.functions.unsign(p))
						p.sendMessage(ChatColor.DARK_GREEN + "Book unsigned!");
					else p.sendMessage(ChatColor.DARK_RED + "You must be holding a written book to use this command!");
				} else p.sendMessage(ChatColor.DARK_RED + "You do not have permission to unsign others' books!");
				return true;
			}
		}
		
		
		
		
		//command: /book a <args> - attempt to change author with additional args. Include spaces.
		if (args.length > 1 && (args[0].equalsIgnoreCase("a") || args[0].equalsIgnoreCase("author"))){
			if (p.hasPermission("booksuite.command.author")){
				String newAuthor = "";
				for (int i = 1; i < args.length; i++)
					if (i != (args.length-1))
						newAuthor += args[i] + " ";
					else newAuthor += args[i];
				if (plugin.functions.setAuthor(p, newAuthor))
					p.sendMessage(ChatColor.DARK_GREEN + "Author changed!");
				else p.sendMessage(ChatColor.DARK_RED + "You must be holding a written book to use this command!");
				return true;
			}
		}
		
		
		
		
		//command: /book t <args> - attempt to change title with additional args. Include spaces.
		if (args.length > 1 && (args[0].equalsIgnoreCase("t") || args[0].equalsIgnoreCase("title"))) {
			if (p.hasPermission("booksuite.command.title")) {
				if (p.getName().equals(((BookMeta) p.getItemInHand().getItemMeta()).getAuthor())) {
					String newTitle = "";
					for (int i = 1; i < args.length; i++)
						if (i != (args.length-1))
							newTitle += args[i] + " ";
						else newTitle += args[i];
					if (plugin.functions.setTitle(p, newTitle)) {
						p.sendMessage(ChatColor.DARK_GREEN + "Title changed!");
					} else p.sendMessage(ChatColor.DARK_RED + "You must be holding a written book to use this command!");
				} else if (p.hasPermission("booksuite.command.title.other")) {
					String newTitle = "";
					for (int i = 1; i < args.length; i++)
						if (i != (args.length - 1))
							newTitle += args[i] + " ";
						else newTitle += args[i];
					if (plugin.functions.setTitle(p, newTitle)) {
						p.sendMessage(ChatColor.DARK_GREEN + "Title changed!");
					} else p.sendMessage(ChatColor.DARK_RED + "You must be holding a written book to use this command!");
				} else p.sendMessage(ChatColor.DARK_RED + "You do not have permission to rename others' books!");
				return true;
			}
		}
		
		
		
		
		//command: /book l(ist) - list all files in /SavedBooks/
		if (args.length == 1 && (args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("ls"))) {
			if (p.hasPermission("booksuite.command.list")) {
				if (args.length == 1) {
					plugin.filemanager.listBookFilesIn(plugin.getDataFolder() + "/SavedBooks/", p);
					return true;
				}
			}
		}
		
		
		
		
		//command: /book <u(rl)|f(ile)|l(oad)> <args> - attempt to import a book from location args[2]
		if (args.length == 2) {
			if ((args[0].equalsIgnoreCase("f") || args[0].equalsIgnoreCase("file") || args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("load")) && p.hasPermission("booksuite.command.import")) {
				ItemStack newbook = new ItemStack(Material.WRITTEN_BOOK, 1);
				newbook.setItemMeta(plugin.filemanager.makeBookMetaFromText(p, args[1], plugin.getDataFolder() + "/SavedBooks/", true));
				if (!newbook.hasItemMeta()) {
					p.sendMessage(ChatColor.DARK_RED + "Error reading book file. Does it exist?");
				} else if (!plugin.functions.canObtainBook(p)) return true;
				else p.getInventory().addItem(newbook);
				return true;
			} else if ((args[0].equalsIgnoreCase("u") || args[0].equalsIgnoreCase("url")) && (p.hasPermission("booksuite.command.import"))) {
				if (!plugin.functions.canObtainBook(p)) return true;
				else asyncBookImport(p.getName(), args[1], plugin.getDataFolder().getPath());
				return true;
			}
		}
		
		
		
		
		//command: /book <e(xport)|s(ave)> <filename> - attempt to save book in hand to file
		if (args.length == 2 && (args[0].equalsIgnoreCase("e") || args[0].equalsIgnoreCase("export") || args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("save"))) {
			if (p.hasPermission("booksuite.command.export")) {
				if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)) {
					p.sendMessage(ChatColor.DARK_RED + "You must be holding a written book to export it!");
					return true;
				}
				BookMeta bm = (BookMeta) p.getItemInHand().getItemMeta();
				if (plugin.filemanager.makeFileFromBookMeta(bm, plugin.getDataFolder() + "/SavedBooks/", args[1], false)) {
					p.sendMessage(ChatColor.DARK_GREEN + "Book saved successfully!");
				} else {
					p.sendMessage(ChatColor.DARK_RED + "A book by this name already exists!");
					if (p.hasPermission("booksuite.command.delete")) {
						p.sendMessage(ChatColor.DARK_RED + "To overwrite it, do \"" + ChatColor.DARK_AQUA + "/book overwrite" + ChatColor.DARK_RED + "\" within 10 seconds.");
						overwritable.put(p.getName(), args[1]);
						syncOverwriteTimer(p);
					}
				}
				return true;
			}
		}
		
		
		
		
		//command: /book <d(elete)> <filename> - attempt to delete file
		if (args.length == 2 && (args[0].equalsIgnoreCase("d") || args[0].equalsIgnoreCase("delete"))) {
			if (p.hasPermission("booksuite.command.delete")) {
				if (args[1].contains(".")) plugin.filemanager.delete(plugin.getDataFolder() + "/SavedBooks/", args[1]);
				else plugin.filemanager.delete(plugin.getDataFolder() + "/SavedBooks/", args[1] + ".book");
				p.sendMessage(ChatColor.DARK_GREEN + "Deleted!");
				return true;
			}
		}
		
		
		
		
		if (args.length >= 1 && args[0].equalsIgnoreCase("overwrite")) {
			if (p.hasPermission("booksuite.command.delete") && p.hasPermission("booksuite.command.export")) {
				if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)) {
					p.sendMessage(ChatColor.DARK_RED + "You must be holding a written book overwrite an existing book!");
					return true;
				} else {
					if (overwritable.containsKey(p.getName())) {
						if (plugin.filemanager.makeFileFromBookMeta((BookMeta) p.getItemInHand().getItemMeta(), plugin.getDataFolder()+"/SavedBooks/", overwritable.get(p.getName()), true)) {
							p.sendMessage(ChatColor.DARK_GREEN + "Book updated successfully!");
						}
						overwritable.remove(p.getName());
						return true;
					} else {
						if (args.length == 2) {
							if (!plugin.filemanager.delete(plugin.getDataFolder() + "/SavedBooks/", args[1])) {
								p.sendMessage(ChatColor.DARK_RED + "You shouldn't make a habit of using overwrite instead of save.");
							}
							if (plugin.filemanager.makeFileFromBookMeta((BookMeta) p.getItemInHand().getItemMeta(), plugin.getDataFolder() + "/SavedBooks/", args[1], true)) {
								p.sendMessage(ChatColor.DARK_GREEN + "Book updated successfully!");
							}
						} else p.sendMessage(ChatColor.DARK_RED + "What are you trying to overwrite? Use the save function.");
						return true;
					}
				}
			}
		}
		
		
		
		
		if (args.length >= 1 && (args[0].equalsIgnoreCase("usage") || args[0].equalsIgnoreCase("help"))) {
			String permittedCommands = listPermittedCommands(p);
			if (permittedCommands.length() > 0) {
				if (args.length == 1) {
					p.sendMessage(ChatColor.DARK_RED + "Please specify a help topic!");
					p.sendMessage(ChatColor.DARK_RED + "Possible topics are as follows:");
					p.sendMessage(ChatColor.DARK_RED + permittedCommands);
				} else {//TODO redo this, short aliases for commands will ruin it
					if (permittedCommands.contains("import"))
						permittedCommands = permittedCommands + "loadfileurl";
					if (permittedCommands.contains("export"))
						permittedCommands = permittedCommands + "save";
					for (String s : args) {
						if (permittedCommands.contains(s.toLowerCase())) {
							if ("printingpress".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.DARK_GREEN + "A " + ChatColor.AQUA + "printing press" + ChatColor.DARK_GREEN + " is made by placing inverted stairs over a crafting table.");
								if (p.hasPermission("booksuite.copy.createpress"))
									p.sendMessage(ChatColor.DARK_GREEN + "Right click the top of a crafting table holding stairs to easily assemble one!");
								p.sendMessage(ChatColor.DARK_GREEN + "To use a press, right click it with a copiable item.");
								p.sendMessage(ChatColor.DARK_GREEN + "Copiables: " + ChatColor.AQUA + "Written Book" + ChatColor.DARK_GREEN+", " + ChatColor.AQUA + "Book and Quill" + ChatColor.DARK_GREEN + ", " + ChatColor.AQUA + "Map");
							} else if ("eraser".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.DARK_GREEN + "An " + ChatColor.AQUA+"eraser" + ChatColor.DARK_GREEN + " is a cauldron. Right click one with a Written Book to unsign!");
								if (!p.hasPermission("booksuite.block.erase.free"))
									p.sendMessage(ChatColor.DARK_GREEN + "Erasing books consumes water.");
							} else if ("edit".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book addpage <number> (text)" + ChatColor.DARK_GREEN + " - add a page to a book");
								p.sendMessage(ChatColor.AQUA + "/book delpage <number>" + ChatColor.DARK_GREEN + " - delete page from book");
							} else if ("addpage".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book addpage <number> (text)" + ChatColor.DARK_GREEN + " - add a page to a book");
							} else if ("delpage".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book delpage <number>" + ChatColor.DARK_GREEN + " - delete page from book");
							} else if ("author".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book a(uthor) <new author>" + ChatColor.DARK_GREEN + " - change author of book in hand.");
							} else if ("title".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book t(itle) <new title>" + ChatColor.DARK_GREEN + " - change title of book in hand");
							} else if ("copy".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book copy <quantity>" + ChatColor.DARK_GREEN + " - Create copies!");
							} else if ("unsign".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book u(nsign)" + ChatColor.DARK_GREEN + " - unsign book in hand.");
							} else if ("importloadfileurl".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book <u(rl)|f(ile)> <url|filename>" + ChatColor.DARK_GREEN + " - import book from file or url");
							} else if ("saveexport".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book <e(xport)|s(ave)> <filename>" + ChatColor.DARK_GREEN + " - export held book to file");
							} else if ("list".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book l(ist)" + ChatColor.DARK_GREEN + " - list all books");
							} else if ("delete".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book d(elete) <file>" + ChatColor.DARK_GREEN + " - delete specified book");
							} else if ("reload".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book reload" + ChatColor.DARK_GREEN + " - reload the plugin");
							} else if ("update".contains(s.toLowerCase())) {
								p.sendMessage(ChatColor.AQUA + "/book update" + ChatColor.DARK_GREEN + " - check for updates");
							}
						} else {
							p.sendMessage(ChatColor.DARK_RED + "Unknown usage topic: " + s + ".");
						}
					}
				}
				return true;
			}
		}
		
		
		p.sendMessage(ChatColor.AQUA + "BookSuite v" + ChatColor.DARK_PURPLE + plugin.version + ChatColor.AQUA + " is enabled!");
		if (listPermittedCommands(p).length() > 0)
			p.sendMessage(ChatColor.DARK_GREEN + "For a list of commands, use " + ChatColor.AQUA + "/book help");
		
		return true;
	}
	
	
	

	
	
	
	public String listPermittedCommands(Player p) {
		StringBuilder sb = new StringBuilder();
		
		if (p.hasPermission("booksuite.copy.self"))
			sb.append("printingpress, ");
		if (p.hasPermission("booksuite.block.erase"))
			sb.append("eraser, ");
		if (p.hasPermission("booksuite.command.edit"))
			sb.append("addpage, delpage, ");
		if (p.hasPermission("booksuite.command.author"))
			sb.append("author, ");
		if (p.hasPermission("booksuite.command.title"))
			sb.append("title, ");
		if (p.hasPermission("booksuite.command.copy"))
			sb.append("copy, ");
		if (p.hasPermission("booksuite.command.unsign"))
			sb.append("unsign, ");
		if (p.hasPermission("booksuite.command.import"))
			sb.append("import, ");
		if (p.hasPermission("booksuite.command.export"))
			sb.append("export, ");
		if (p.hasPermission("booksuite.command.list"))
			sb.append("list, ");
		if (p.hasPermission("booksuite.command.delete"))
			sb.append("delete, ");
		if (p.hasPermission("booksuite.command.reload"))
			sb.append("reload, ");
		if (p.hasPermission("booksuite.command.update"))
			sb.append("update, ");
		
		return sb.substring(0, sb.length() - 2);
	}
	
	
	
	@SuppressWarnings("deprecation")
	public void asyncBookImport(String p, String s, String dir) {
		Bukkit.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new getStreamBook(p, s, dir));
	}
	
	public class getStreamBook implements Runnable {
		String p;
		URL url;
		String loc;
		getStreamBook(String p, String s, String dir) {
			this.p = p;
			loc = dir;
			try {
				url = new URL(s);
			} catch (MalformedURLException e) {
			}
		}
		public void run() {
			File dir = new File(loc+"/temp/");
			if (!dir.exists())
				dir.mkdirs();
			File tempFile;
			for (int i = 1; i <= 5; i++) {
				tempFile = new File(dir, "temp" + i + ".book");
				if (!tempFile.exists()){
					try {
						tempFile.createNewFile();
						Scanner urlInput = new Scanner(url.openStream());
						FileWriter tempWriter = new FileWriter(tempFile);
						while (urlInput.hasNextLine()) {
							tempWriter.append(urlInput.nextLine()+"\n");
						}
						urlInput.close();
						tempWriter.close();
					} catch (Exception e) {
						if(tempFile.exists())
							tempFile.delete();
						return;
					}
					syncBookImport(p, i);
					return;
				} else if (i == 5)
					syncBookImport(p, -1);
			}
		}
	}
	
	public void syncBookImport(String p, int temp) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new giveStreamBook(p, temp));
	}
	
	public class giveStreamBook implements Runnable {
		Player p;
		int temp;
		FileManager fm = FileManager.getInstance();
		giveStreamBook(String p, int temp) {
			this.p = plugin.getServer().getPlayer(p);
			this.temp = temp;
		}
		public void run() {
			if (temp == -1) {
				p.sendMessage(ChatColor.DARK_RED+"Too many books are being imported at this time, please try again later.");
				return;
			}
			BookMeta bm = fm.makeBookMetaFromText(p, "temp" + temp, plugin.getDataFolder() + "/temp/", true);
			fm.delete(plugin.getDataFolder() + "/temp/", "temp" + temp + ".book");
			if (bm != null) {
				ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
				is.setItemMeta(bm);
				if (p.getInventory().firstEmpty() != -1) {
					p.getInventory().addItem(is);
				} else {
					p.getWorld().dropItem(p.getLocation(), is);
				}
			}
			else {
				p.sendMessage(ChatColor.DARK_RED + "Error reading from URL.");
				if(p.getInventory().firstEmpty() > 0) {
					p.getInventory().addItem(new ItemStack(Material.INK_SACK, 1));
					p.getInventory().addItem(new ItemStack(Material.BOOK, 1));
				} else {
					p.sendMessage(ChatColor.DARK_RED + "Dropped book supplies at your feet.");
					p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.INK_SACK, 1));
					p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.BOOK, 1));
				}
			}
		}
	}
	
	
	
	public void syncOverwriteTimer(Player p) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new overwriteTimer(p), 200L);
	}
	
	public class overwriteTimer implements Runnable {
		Player p;
		overwriteTimer(Player p) {
			this.p = p;
		}
		public void run() {
			if(overwritable.containsKey(p.getName())) {
				overwritable.remove(p.getName());
				p.sendMessage(ChatColor.DARK_RED+"Overwrite time expired!");
			}
		}
	}
}
