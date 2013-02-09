package com.github.Jikoo.BookSuite;

import java.io.File;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookSuiteMailExecutor {
	
	public static boolean sendMail(Player p, BookMeta bm, String pluginDataFolder, boolean usePermissions){
		bm = (BookMeta) p.getItemInHand().getItemMeta();
		if (!p.getName().equals(bm.getAuthor())&&!(p.hasPermission("booksuite.mail.send.other")||(usePermissions&&p.isOp())))
			p.sendMessage(ChatColor.DARK_RED+p.getName()+", you shouldn't be trying to send letters for "+bm.getAuthor()+"...");
		else{
			boolean mailHasItemAttached = false;
			String[] sendingData = parseSendingData(bm.getPage(1));
			
			BookMeta newBook = (BookMeta) new ItemStack(Material.WRITTEN_BOOK).getItemMeta();
			newBook.setAuthor(bm.getAuthor());
			Inventory inv = p.getInventory();
			ItemStack removeThis = new ItemStack(Material.DIRT, 1);
			boolean playerHasItem = false;
			for (int i=2; i<=bm.getPageCount(); i++)
				newBook.addPage(bm.getPage(i));
			if (sendingData[2]!=null&&sendingData[2]!=""){
				mailHasItemAttached=true;
				newBook.setTitle("Package: "+sendingData[0]);
				newBook.addPage("To: "+sendingData[1]+"\nAttached:\n"+sendingData[2]);
				for (ItemStack is:inv.getContents())
					if (is!=null)
						if (is.hasItemMeta())
							if(is.getItemMeta().hasDisplayName())
								if (is.getItemMeta().getDisplayName().equalsIgnoreCase(sendingData[2])){
									removeThis = is;
									playerHasItem = true;
								}
			} else newBook.setTitle(sendingData[0]);
			
			if (mailHasItemAttached && !playerHasItem){
				p.sendMessage(ChatColor.DARK_RED+"Error: no such named item, please check spelling.");
				BookSuiteFunctions.unsign(p);
				return false;
			}
			
			
			if(BookSuiteFileManager.appendMailIndex(pluginDataFolder+"/Mail/"+sendingData[1]+"/", sendingData[0])){
				if(new File(pluginDataFolder+"/Mail/"+sendingData[1]+"/Books/", sendingData[0]).exists()){
					if (mailHasItemAttached && playerHasItem){
						if(BookSuiteFileManager.makeFileFromItemStack(removeThis, pluginDataFolder+"/Mail/"+sendingData[1]+"/Items/", sendingData[2]))
							inv.remove(removeThis);
						else{
							p.sendMessage(ChatColor.DARK_RED+"Error: "+sendingData[1]+" already has an item by that name in their mailbox.");
							BookSuiteFunctions.unsign(p);
							return false;
						}
					}
					
					
					if(BookSuiteFileManager.makeFileFromBookMeta(newBook, pluginDataFolder+"/Mail/"+sendingData[1]+"/Books/", sendingData[0])){
						inv.remove(p.getItemInHand());
						p.sendMessage(ChatColor.DARK_GREEN+"Mail sent successfully!");
						return true;
					}
				} else p.sendMessage(ChatColor.DARK_RED+"Error: "+sendingData[1]+" already has a book by that name in their mailbox.");
			} else p.sendMessage(ChatColor.DARK_RED+"Error writing mail index!");
		}
		return false;
	}
	
	
	
	
	public static boolean loadMail(Player p, BookMeta bm, String pluginDataFolder){
		bm = (BookMeta) p.getItemInHand().getItemMeta();
		String[] checks = bm.getPage(bm.getPageCount()).replace("To: ", "").replace("Attached:\n", "").split("\n");
		if (p.getName()==checks[0]){
			if(p.getInventory().firstEmpty()!= -1){
				bm.setTitle(bm.getTitle().replace("Package: ", ""));
				bm.setPage(bm.getPageCount(), "Attached:\n"+checks[1]);
				BookSuiteFileManager.makeItemStackFromFile(pluginDataFolder+"/Mail/"+p.getName()+"/Items/", checks[1]);
				BookSuiteFileManager.delete(pluginDataFolder+"/Mail/"+p.getName()+"/Items/", checks[1]);
				return true;
			}
			p.sendMessage(ChatColor.DARK_RED+"You do not have space to unpack this book.");
		}
		return false;
	}
	
	
	
	
	
	
	public static Inventory getMailBoxInv(Player p, String pluginDataFolder){
		Inventory mailbox =  Bukkit.createInventory(p, 2, p.getDisplayName()+"'s MailBox");
		Scanner s = new Scanner(pluginDataFolder+"/Mail/index.bsm");
		while (s.hasNext()){
			if (mailbox.firstEmpty()!=-1){
				ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
				is.setItemMeta(BookSuiteFileManager.makeBookMetaFromText(p, s.nextLine()+".book",pluginDataFolder+"/Mail/"+p.getName()+"/Books/", true));
				mailbox.addItem(is);
				//TODO delete + remove - on inventory close, though. foreach isempty add to int[] removeEntry for 0 to <27 if s.hasnext foreach int in removeEntry if == i s.don'taddline
			}
			
		}
		s.close();
		return mailbox;
	}




	public static String[] parseSendingData(String firstPage){
		String[] pageData = firstPage.split("\n");
		pageData[0] = pageData[0].replaceFirst("\\A.*([Pp]ackage|[Tt]itle):\\s*", "").replaceAll("\\W", "");
		pageData[1] = pageData[1].replaceFirst("\\A.*[Tt]o:\\s*", "").replaceAll("\\W", "");
		if (pageData[2]!=null){
			pageData[2] = pageData[2].replaceFirst("\\A.*([Ii]tem|[Aa]ttach):\\s*", "");
			if(pageData[2].equalsIgnoreCase("n/a")||pageData[2].equalsIgnoreCase("none")||pageData[2].equalsIgnoreCase("nothing"))
				pageData[2]="";
		}
		return pageData;
	}
	public String parseReceivingData(String lastpage){
		String toItem = lastpage.replace("To: ", "").replace("Item: ", "");
		
		
		return toItem;
	}
}