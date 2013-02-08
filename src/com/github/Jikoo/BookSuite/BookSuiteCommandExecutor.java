package com.github.Jikoo.BookSuite;


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
		if (args.length==1&&(args[0].equalsIgnoreCase("eraser")||args[0].equalsIgnoreCase("e"))){
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
		}
		
		
		//command: /book u - attempt to unsign book
		if (args.length==1&&(args[0].equalsIgnoreCase("unsign")||args[0].equalsIgnoreCase("u"))){
			if (p.hasPermission("booksuite.command.unsign")||(!plugin.usePermissions&&p.isOp())){
				if(BookSuiteFunctions.unsign(p))
					p.sendMessage(ChatColor.DARK_GREEN+"Book unsigned!");
				else p.sendMessage(ChatColor.DARK_RED+"You must be holding a written book to use this command!");
				return true;
			}
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
		}
		
		
		//command: /book t <args> - attempt to change title with additional args. Include spaces.
		if (args.length > 1&&(args[0].equalsIgnoreCase("t")||args[0].equalsIgnoreCase("title"))){
			if (p.hasPermission("booksuite.command.title")||(!plugin.usePermissions&&p.isOp())){
				String newTitle = "";
				for (int i=1; i<args.length; i++)
					if (i!=(args.length-1))
						newTitle+=args[i]+" ";
					else newTitle+=args[i];
				if(BookSuiteFunctions.setAuthor(p, newTitle))
					p.sendMessage(ChatColor.DARK_GREEN+"Author changed!");
				else p.sendMessage(ChatColor.DARK_RED+"You must be holding a written book to use this command!");
				return true;
			}
		}
		
		
		
		//command: /book <u(rl)|f(ile)> <args> - attempt to import a book from location args[2]
		if (args.length == 3&&(args[0].equalsIgnoreCase("u")||args[0].equalsIgnoreCase("f")||args[0].equalsIgnoreCase("url")||args[0].equalsIgnoreCase("file"))){
			if (p.hasPermission("booksuite.command.import")||!plugin.usePermissions){
				if (!plugin.canObtainBook(p)) return true;
				ItemStack newbook = new ItemStack(Material.WRITTEN_BOOK, 1);
				newbook.setItemMeta(BookSuiteFileManager.makeBookMetaFromText(args[2], plugin.getDataFolder()+"/SavedBooks/", args[1]));
				if (!newbook.hasItemMeta()){
					p.sendMessage(ChatColor.DARK_RED+"Error reading book file.");
				}
				else p.getInventory().addItem(newbook);
				return true;
			}
		}
		
		
		
		//command: /book <e(xport)|s(ave)> <filename> - attempt to save book in hand to file
		if (args.length == 2&&(args[0].equalsIgnoreCase("e")||args[0].equalsIgnoreCase("export")||args[0].equalsIgnoreCase("s")||args[0].equalsIgnoreCase("save"))){
			if (p.hasPermission("booksuite.command.export")||!plugin.usePermissions){
				if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK))
					p.sendMessage(ChatColor.DARK_RED+"You must be holding a written book to export it!");
				BookMeta bm = (BookMeta) p.getItemInHand().getItemMeta();
				BookSuiteFileManager.makeFileFromBookMeta(bm, plugin.getDataFolder()+"/SavedBooks/", args[1]);
				return true;
			}
		}
		
		
		
		
		//if no commands match, print out help based on permissions
		p.sendMessage(ChatColor.DARK_BLUE+"B"+ChatColor.AQUA+"ook"+ChatColor.DARK_BLUE+"S"+ChatColor.AQUA+"uite "+ChatColor.DARK_BLUE+"v"+ChatColor.AQUA+plugin.version+ChatColor.DARK_GREEN+" usage:");
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
		return true;
	}
	
	
}
