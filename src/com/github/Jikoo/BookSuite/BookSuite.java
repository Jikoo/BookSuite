package com.github.Jikoo.BookSuite;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.Jikoo.BookSuite.metrics.Metrics;
import com.github.Jikoo.BookSuite.permissions.PermissionsListener;


public class BookSuite extends JavaPlugin implements Listener {
	String version = "3.0.0";
	PermissionsListener perms;
	Functions functions = new Functions();
	FileManager filemanager = new FileManager();
	MailExecutor mail = new MailExecutor();
	Metrics metrics;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		try {
			
			getConfig().addDefault("use-inbuilt-permissions", false);
			
			if (getConfig().getBoolean("use-inbuilt-permissions")) {
				perms = new PermissionsListener(this);
				perms.enable();
				
			}
			
			getConfig().addDefault("enable-metrics", true);
			
			//Let's allow players to more easily disable our metrics
			if (getConfig().getBoolean("enable-metrics")) {
				try {
					metrics = new Metrics(this);
					metrics.start();
				} catch (IOException e) {
					getLogger().warning("[BookSuite] Error enabling metrics: " + e);
					e.printStackTrace();
					getLogger().warning("[BookSuite] End error report.");
					if (metrics != null) {
						metrics.disable();
						metrics = null;
					}
				}
			}
		} catch (Exception e) {
			getLogger().warning("[BookSuite] Error loading configuration: " + e);
			e.printStackTrace();
			getLogger().warning("[BookSuite] End error report.");
		}
		
		if (new File(getDataFolder(), "temp").exists())
			filemanager.delete(getDataFolder().getPath(), "temp");
		
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("book").setExecutor(new CommandHandler(this));
		getLogger().info("BookSuite v"+version+" enabled!");
	}
	
	
	
	
	
	@Override
	public void onDisable() {
		if (new File(getDataFolder(), "temp").exists())
			filemanager.delete(getDataFolder().getPath(), "temp");
		perms = null;
		getLogger().info("BookSuite v"+version+" disabled!");
	}


	/**
	 * copy book (PrintingPress) or send mail
	 * 
	 * @param event world triggered event
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

			Player p = event.getPlayer();
			ItemStack is = p.getItemInHand();
			Block clicked = event.getClickedBlock();
			Block blockUp = clicked.getRelative(BlockFace.UP);
			
			
			
			//if clicking a workbench, check to see if it is a press and act accordingly
			if (clicked.getType().equals(Material.WORKBENCH)) {
				if (functions.isInvertedStairs(blockUp)) {
					PrintingPress press = new PrintingPress(this, p, is, blockUp);
					if (!press.denyUseage()) {
						if (is.getType().equals(Material.MAP)) {
							if (functions.canObtainMap(p))
								press.operatePress();
							event.setCancelled(true);
						} else if (!(is.hasItemMeta() || is.getItemMeta()!=null)) {
							return;
						} else if (is.getType().equals(Material.WRITTEN_BOOK)) {
							BookMeta bm = (BookMeta) is.getItemMeta();
							if (press.checkCopyPermission(bm.getAuthor()) && functions.canObtainBook(p))
								press.operatePress();
							event.setCancelled(true);
						} else if (is.getType().equals(Material.BOOK_AND_QUILL)) {
							if (p.hasPermission("booksuite.copy.unsigned")) {
								if (functions.canObtainBook(p))
									press.operatePress();
							} else p.sendMessage(ChatColor.DARK_RED+"You do not have permission to copy unsigned books!");
							event.setCancelled(true);
						}
					}
				} else if (event.getBlockFace().equals(BlockFace.UP) && blockUp.isEmpty() && functions.isCorrectStairType(is)) {
					if (p.hasPermission("booksuite.copy.createpress")) {
						blockUp.setTypeIdAndData(is.getTypeId(), functions.getCorrectStairOrientation(p), true);
						if (is.getAmount() == 1)
							p.setItemInHand(null);
						else is.setAmount(is.getAmount()-1);
						event.setCancelled(true);
						p.updateInventory();
					}
				}
			} else if (is.getType().equals(Material.WRITTEN_BOOK)) {
				if (!(is.hasItemMeta() || is.getItemMeta()!=null))
					return;
				if (clicked.getType().equals(Material.CAULDRON)) {
					BookMeta bm = (BookMeta) is.getItemMeta();
					if (p.hasPermission("booksuite.block.erase")) {
						if (clicked.getData() < 1 && !p.getGameMode().equals(GameMode.CREATIVE) && !p.hasPermission("booksuite.block.erase.free"))
							p.sendMessage(ChatColor.DARK_RED+"You'll need some water to unsign this book.");
						else if (bm.getAuthor().equalsIgnoreCase(p.getDisplayName())) {
							functions.unsign(p);
							if (!p.hasPermission("booksuite.block.erase.free") && !p.getGameMode().equals(GameMode.CREATIVE))
								clicked.setData((byte) (clicked.getData()-1));
						} else if (p.hasPermission("booksuite.block.erase.other")) {
							functions.unsign(p);
							if(!p.hasPermission("booksuite.block.erase.free") && !p.getGameMode().equals(GameMode.CREATIVE))
								clicked.setData((byte) (clicked.getData()-1));
						} else p.sendMessage(ChatColor.DARK_RED+"You can only unsign your own books.");
						event.setCancelled(true);
					} else if (!p.hasPermission("booksuite.denynowarn.erase")) {
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
						p.openInventory(mail.getMailBoxInv(p, this.getDataFolder().getPath()));
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
					if(mail.loadMail(p, bm, this.getDataFolder().getPath()))
						event.setCancelled(true);
				}
				else if (p.hasPermission("booksuite.mail.send")&&bm.getTitle().equalsIgnoreCase("package"))
					if (mail.sendMail(p, bm, this.getDataFolder().getPath()))
						event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose (InventoryCloseEvent event){
		if (event.getInventory().getTitle().contains("'s MailBox")){
			mail.WriteMailContents(event.getInventory());
		}
	}
}
