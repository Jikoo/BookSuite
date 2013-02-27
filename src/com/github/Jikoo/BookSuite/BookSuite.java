package com.github.Jikoo.BookSuite;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;


public class BookSuite extends JavaPlugin implements Listener{
	String version = "3.0.0";
	Boolean usePermissions;
	String neededSupplies = "";
	
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		FileConfiguration fc = getConfig();
		try {
			usePermissions = fc.getBoolean("usePermissions");
		} catch (Exception e) {
		}
		if(new File(getDataFolder(), "temp").exists())
			BookSuiteFileManager.delete(getDataFolder().getPath(), "temp");
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("book").setExecutor(new BookSuiteCommandExecutor(this));
		getLogger().info("BookSuite v"+version+" enabled!");
	}
	
	
	
	
	
	@Override
	public void onDisable() {
		if(new File(getDataFolder(), "temp").exists())
			BookSuiteFileManager.delete(getDataFolder().getPath(), "temp");
		getLogger().info("BookSuite v"+version+" disabled!");
	}


	/**
	 * copy book (PrintingPress) or send mail
	 * 
	 * @param event world triggered event
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){

			Player p = event.getPlayer();
			ItemStack is = p.getItemInHand();
			Block clicked = event.getClickedBlock();
			Block blockUp = clicked.getRelative(BlockFace.UP);
			
			
			if (is.getType().equals(Material.WRITTEN_BOOK)){
				if(!(is.hasItemMeta()||is.getItemMeta()!=null))
					return;
				//if clicking a workbench, check to see if it is a press and act accordingly
				if(clicked.getType().equals(Material.WORKBENCH)){
					BookSuitePrintingPress press = new BookSuitePrintingPress(this, p, is, blockUp);
					if (BookSuitePrintingPress.isInvertedStairs(blockUp) && !press.denyUseage()){
						
						BookMeta bm = (BookMeta) is.getItemMeta();
						if (press.checkCopyPermission(bm.getAuthor()) && BookSuiteFunctions.canObtainBook(p, usePermissions))
							press.operatePress();
						event.setCancelled(true);
					}
				} else if (clicked.getType().equals(Material.CAULDRON)){
					BookMeta bm = (BookMeta) is.getItemMeta();
					if(!usePermissions){
						event.setCancelled(true);
						p.closeInventory();
						if (!p.isOp()){
							if (clicked.getData()<1)
								p.sendMessage(ChatColor.DARK_RED+"You'll need some water to unsign this book.");
							else if(!bm.getAuthor().equalsIgnoreCase(p.getDisplayName()))
								p.sendMessage(ChatColor.DARK_RED+"You can only unsign your own books.");
							else {
								BookSuiteFunctions.unsign(p);
								if(!p.getGameMode().equals(GameMode.CREATIVE))
									clicked.setData((byte) (clicked.getData()-1));
							}
						} else BookSuiteFunctions.unsign(p);
					} else if(p.hasPermission("booksuite.block.erase")){
						if (clicked.getData()<1&&!p.getGameMode().equals(GameMode.CREATIVE)&&!p.hasPermission("booksuite.block.erase.free"))
							p.sendMessage(ChatColor.DARK_RED+"You'll need some water to unsign this book.");
						else if(bm.getAuthor().equalsIgnoreCase(p.getDisplayName())){
							BookSuiteFunctions.unsign(p);
							if(!p.hasPermission("booksuite.block.erase.free")&&!p.getGameMode().equals(GameMode.CREATIVE))
								clicked.setData((byte) (clicked.getData()-1));
						}
						else if (p.hasPermission("booksuite.block.erase.other")){
							BookSuiteFunctions.unsign(p);
							if(!p.hasPermission("booksuite.block.erase.free")&&!p.getGameMode().equals(GameMode.CREATIVE))
								clicked.setData((byte) (clicked.getData()-1));
						}
						else p.sendMessage(ChatColor.DARK_RED+"You can only unsign your own books.");
						event.setCancelled(true);
					} else if (!p.hasPermission("booksuite.denynowarn.erase")){
						p.sendMessage(ChatColor.DARK_RED+"You do not have permission to use erasers.");
						event.setCancelled(true);
					}
				}
			}
			
			
			
			//this is for checking mail
			if (clicked.getType().equals(Material.CHEST))
				if (blockUp.getType().equals(Material.SIGN)) {
					Sign sign = (Sign) blockUp;
					if (sign.getLine(0).equals(ChatColor.DARK_RED+"No sign line can contain this string.")){//rudimentary example
						p.openInventory(BookSuiteMailExecutor.getMailBoxInv(p, this.getDataFolder().getPath()));
						event.setCancelled(true);
					}
				} 
			
		}
		
		
		// this is for taking a "package/envelope" that contains a "gift" and opening it into your inventory.
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR)){
			Player p = event.getPlayer();
			if (p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)){
				if(!(p.getItemInHand().hasItemMeta()||p.getItemInHand().getItemMeta()!=null))
					return;
				BookMeta bm = (BookMeta) p.getItemInHand().getItemMeta();
				if (bm.getTitle().contains("Package: ")){
					if(BookSuiteMailExecutor.loadMail(p, bm, this.getDataFolder().getPath()))
						event.setCancelled(true);
				}
				else if (p.hasPermission("booksuite.mail.send")&&bm.getTitle().equalsIgnoreCase("package"))
					if (BookSuiteMailExecutor.sendMail(p, bm, this.getDataFolder().getPath(), usePermissions))
						event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClick (InventoryClickEvent event){
		Player p = (Player) event.getWhoClicked();
		Inventory i = event.getInventory();
		//Is it a mailbox? If true, cancel all clicks, handle from there.
		if (i.getTitle().equals(p.getName()+"'s MailBox")){
			event.setCancelled(true);
			if (event.getCurrentItem()==null){
				return;
			} else {
				if (p.getInventory().firstEmpty()!=-1){
					if (event.getCurrentItem().getType().equals(Material.WRITTEN_BOOK))
						p.getInventory().addItem(event.getCurrentItem().clone());
						event.getInventory().remove(event.getCurrentItem());
						BookMeta bm = (BookMeta) event.getCurrentItem().getItemMeta();
						BookSuiteFileManager.removeMail(this.getDataFolder()+"/Mail/"+p.getName()+"/", bm.getTitle().replaceAll("Package: ", ""));
						p.updateInventory();
				} else {
					p.sendMessage(ChatColor.DARK_RED+"You need to free up space to withdraw mail!");
					p.closeInventory();
				}
			}
		}
		//if the player clicks own inventory while viewing their mailbox, cancel + send update
		if (i.equals(p.getInventory())){
			if (p.getOpenInventory()!=null){
				if (p.getOpenInventory().getTitle().equals(p.getName()+"'s MailBox")){
					event.setCancelled(true);
					p.updateInventory();
				}
			}
		}
	}
}
