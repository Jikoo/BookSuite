package com.github.Jikoo.BookSuite;

import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class BookSuiteMailExecutor {
	
BookSuite plugin;
Player p;
String to;
String title;
String itemMetaName;
BookMeta bm;
boolean mailHasItemAttached;
Event event;
	
	public BookSuiteMailExecutor(BookSuite plugin, Player p, Event e){
		this.plugin = plugin;
		this.p = p;
		this.event = e;
	}



	public boolean sendMail(){
		bm = (BookMeta) p.getItemInHand().getItemMeta();
		p.sendMessage("Debug: Checking perms");
		if (p.getName()!=bm.getAuthor())
			p.sendMessage(ChatColor.DARK_RED+p.getName()+", you shouldn't be trying to send letters for "+bm.getAuthor()+"...");
		else{
			p.sendMessage("Debug: Parsing mail");
			parseSendingData();
			p.sendMessage("Debug: Parsed: title "+title+", to "+to+", item "+itemMetaName);
			p.sendMessage("Debug: Instantiating variables");
			BookMeta newBook = (BookMeta) new ItemStack(Material.WRITTEN_BOOK).getItemMeta();
			newBook.setAuthor(bm.getAuthor());
			Inventory inv = p.getInventory();
			ItemStack removeThis = new ItemStack(Material.DIRT, 1);
			boolean playerHasItem = false;
			p.sendMessage("Debug: making new bookmeta");
			for (int i=2; i<=bm.getPageCount(); i++)
				newBook.addPage(bm.getPage(i));
			if (itemMetaName!=null&&itemMetaName!=""){
				p.sendMessage("Debug: Checking for item");
				newBook.setTitle("Package: "+title);
				newBook.addPage("To: "+to+"\nAttached:\n"+itemMetaName);
				int i=0;
				for (ItemStack is:inv.getContents()){
					i++;
					if (is!=null){
						p.sendMessage("Debug: iteration "+i+" itemstack is "+is.getType().toString());
						if (is.hasItemMeta())
							if(is.getItemMeta().hasDisplayName())
								if (is.getItemMeta().getDisplayName().equalsIgnoreCase(itemMetaName)){
									removeThis = is;
									playerHasItem = true;
									p.sendMessage("Debug: Have item!");
								}
					} else p.sendMessage("Iteration "+i+" null");
				}
			} else newBook.setTitle(title);
			p.sendMessage("mail has item: "+mailHasItemAttached+" player has item specified: "+playerHasItem);
			
			if (mailHasItemAttached && !playerHasItem){
				p.sendMessage(ChatColor.DARK_RED+"Error: no such named item, please check spelling.");
				BookSuiteFunctions.unsign(p);
				return false;
			}
			
			p.sendMessage("Debug: handling writing items");
			if(BookSuiteFileManager.appendMailIndex(plugin.getDataFolder()+"/Mail/"+to, title)){
				if (mailHasItemAttached && playerHasItem){
					BookSuiteFileManager.makeFileFromItemStack(removeThis, plugin.getDataFolder()+"/Mail/"+to+"/Items/", itemMetaName+".item");
					inv.remove(removeThis);
				}
				
				
				BookSuiteFileManager.makeFileFromBookMeta(newBook, plugin.getDataFolder()+"/Mail/"+to+"/Books/", title+".book");
				inv.remove(p.getItemInHand());
				p.sendMessage(ChatColor.DARK_GREEN+"Mail sent successfully!");
				return true;
			} else p.sendMessage(ChatColor.DARK_RED+"Error writing mail index!");
		}
		p.sendMessage("Debug: send operation finished, failed.");
		return false;
	}
	
	
	
	
	public boolean loadMail(){
		bm = (BookMeta) p.getItemInHand().getItemMeta();
		String[] checks = bm.getPage(bm.getPageCount()).replace("To: ", "").replace("Attached:\n", "").split("\n");
		if (p.getName()==checks[0]){
			if(p.getInventory().firstEmpty()!= -1){
				bm.setTitle(bm.getTitle().replace("Package: ", ""));
				bm.setPage(bm.getPageCount(), "Attached:\n"+checks[1]);
				BookSuiteFileManager.makeItemStackFromFile(plugin.getDataFolder()+"/Mail/"+to+"/Items/", checks[1]);
				BookSuiteFileManager.delete(plugin.getDataFolder()+"/Mail/"+to+"/Items/", checks[1]);
				return true;
			}
			p.sendMessage(ChatColor.DARK_RED+"You do not have space to unpack this book.");
		}
		return false;
	}
	
	
	
	
	
	
	public static Inventory getMailBoxInv(Player p, BookSuite plugin){
		Inventory mailbox =  Bukkit.createInventory(p, 2, p.getDisplayName()+"'s MailBox");
		Scanner s = new Scanner(plugin.getDataFolder()+"/Mail/index.bsm");
		while (s.hasNext()){
			if (mailbox.firstEmpty()!=-1){
				ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
				is.setItemMeta(BookSuiteFileManager.makeBookMetaFromText(s.nextLine()+".book", plugin.getDataFolder()+"/Mail/"+p.getName()+"/Books/", ""));
				mailbox.addItem(is);
				//TODO delete + remove - on inventory close, though. foreach isempty add to int[] removeEntry for 0 to <27 if s.hasnext foreach int in removeEntry if == i s.don'taddline
			}
			
		}
		s.close();
		return mailbox;
	}




	public void parseSendingData(){
		mailHasItemAttached = false;
		String[] pageData = bm.getPage(1).split("\n");
		title = pageData[0].replaceFirst("\\A.*([Pp]ackage|[Tt]itle):\\s*", "").replaceAll("\\W", "");
		to = pageData[1].replaceFirst("\\A.*[Tt]o:\\s*", "").replaceAll("\\W", "");
		if (pageData[2]!=null){
			pageData[2] = pageData[2].replaceFirst("\\A.*([Ii]tem|[Aa]ttach):\\s*", "");
			if(pageData[2].equalsIgnoreCase("n/a")||pageData[2].equalsIgnoreCase("none")||pageData[2].equalsIgnoreCase("nothing"))
				pageData[2]="";
			if(pageData[2]!="") mailHasItemAttached = true;
		}
		itemMetaName = pageData[2];
	}
	public String parseReceivingData(String lastpage){
		String toItem = lastpage.replace("To: ", "").replace("Item: ", "");
		
		
		return toItem;
	}
}