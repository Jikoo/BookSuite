package com.github.Jikoo.BookSuite;

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
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("makebook").setExecutor(new BookSuiteCommandExecutor(this));
		getLogger().info("BookSuite v"+version+" enabled!");
	}





	@Override
	public void onDisable() {
		getLogger().info("BookSuite v"+version+" disabled!");
	}





	/**
	 * checks if the player has the supplies needed
	 * 
	 * @param inv the inventory of the player
	 * @return whether the player has the supplies needed to copy the book
	 */
	public boolean hasSupplies(Inventory inv){
		if (inv.contains(Material.BOOK) && inv.contains(Material.INK_SACK)) return true;
		if (inv.contains(Material.BOOK)) neededSupplies = "an ink sack";
		else if (inv.contains(Material.INK_SACK)) neededSupplies = "a book";
		else neededSupplies = "a book and an ink sack";
		return false;
	}





	/**
	 * master method for checking if the player can obtain the books
	 *
	 * @param p the player attempting to obtain the book
	 * @return whether the player can obtain the book
	 */
	public boolean canObtainBook(Player p){
		Inventory inv = p.getInventory();

		if (p.hasPermission("booksuite.free") || p.getGameMode().equals(GameMode.CREATIVE) || (!usePermissions && p.isOp())){
			if (inv.firstEmpty()==-1){
				p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
				return false;
			}
			return true;
		}

		if (hasSupplies(inv)){
			inv.removeItem(new ItemStack(Material.INK_SACK, 1));
			inv.removeItem(new ItemStack(Material.BOOK, 1));
			if (inv.firstEmpty()==-1){
				p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
				inv.addItem(new ItemStack(Material.INK_SACK, 1));
				inv.addItem(new ItemStack(Material.BOOK, 1));
				return false;
			}
			return true;
		}

		p.sendMessage(ChatColor.DARK_RED+"To create a book, you need "+neededSupplies+".");
		return false;
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


			if (is.getType().equals(Material.WRITTEN_BOOK) && clicked.getType().equals(Material.WORKBENCH)){
				BookSuitePrintingPress press = new BookSuitePrintingPress(this, p, is, blockUp);
				if (BookSuitePrintingPress.isInvertedStairs(blockUp) && !press.denyUseage()){

					BookMeta bm = (BookMeta) is.getItemMeta();
					if (press.checkCopyPermission(bm.getAuthor()) && canObtainBook(p))
						press.operatePress();
					event.setCancelled(true);
				}
			}
			
			
			
			//this is for checking mail
			if (clicked.getType().equals(Material.CHEST))
				if (blockUp.getType().equals(Material.SIGN)) {
					Sign sign = (Sign) blockUp;
					if (sign.getLine(0).equals(ChatColor.DARK_RED+"No sign line can contain this string.")){//rudimentary example
						p.openInventory(BookSuiteMailExecutor.getMailBoxInv(p, this));
						event.setCancelled(true);
					}
				} 
			
		}


		// this is for taking a "package/envelope" that contains a "gift" and opening it into your inventory.
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR)){
			Player p = event.getPlayer();
			if (p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)){
				BookMeta bm = (BookMeta) p.getItemInHand().getItemMeta();
				if (bm.getTitle().contains("Package: ")){
					if(new BookSuiteMailExecutor(this, p, event).loadMail())
						event.setCancelled(true);
				}
				else if (p.hasPermission("booksuite.mail.send"))
					if (new BookSuiteMailExecutor(this, p, event).sendMail())
						event.setCancelled(true);
			}
			
		}
		

	}
}
