package com.github.Jikoo.BookSuite;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
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
	String neededSupplies = "";
	BlockState originalBlock;
	BlockState newBlock;
	Boolean usePermissions;


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

		p.sendMessage(ChatColor.DARK_RED+"To copy a book, you need "+neededSupplies+".");
		return false;
	}





	/**
	 * 
	 * @param p
	 * @param a
	 * @return
	 */
	public boolean checkPermission(Player p, String a){
		if (usePermissions){
			if (p.hasPermission("booksuite.copy.other"))
				return true;
			if (p.hasPermission("booksuite.copy.self") && a.equals(p.getName()))
				return true;
			else if (p.hasPermission("booksuite.copy.self"))
				p.sendMessage(ChatColor.DARK_RED+"You do not have permission to copy others' books.");
			else
				p.sendMessage(ChatColor.DARK_RED+"You do not have permission to copy books.");
		}
		else if (p.isOp())
			return true;
		else if (a.equals(p.getName()))
			return true;
		else p.sendMessage(ChatColor.DARK_RED+"Only ops can copy others' books.");
		return false;
	}
	public boolean denyUseage(Player p){
		if (p.hasPermission("booksuite.copy.deny.nowarn"))
			return true;
		return false;
	}






	/**
	 * 
	 * @param p
	 * @param is
	 * @param b
	 */
	public void operatePress(Player p, ItemStack is, Block b){
		originalBlock = b.getState();
		changeStairBlock(b);
		revertBlockPause(b);
		p.getInventory().addItem(is.clone());
		p.updateInventory();
		p.sendMessage(ChatColor.DARK_GREEN+"Book copied!");
	}





	/**
	 * turns the stair block into a slab for graphical effect
	 * 
	 * @param b the stair block to be transformed
	 */
	public void changeStairBlock(Block b){
		if (b.getTypeId() == 53)//WOOD_STAIRS
			b.setTypeIdAndData(126, (byte) 0, false);//WOOD_STEP

		else if (b.getTypeId() == 67)//COBBLESTONE_STAIRS
			b.setTypeIdAndData(44, (byte) 3, false);//STEP

		else if (b.getTypeId() == 108)//BRICK_STAIRS
			b.setTypeIdAndData(44, (byte) 4, false);//STEP

		else if (b.getTypeId() == 109)//SMOOTH_STAIRS
			b.setTypeIdAndData(44, (byte) 5, false);//STEP

		else if (b.getTypeId() == 114)//NETHER_BRICK_STAIRS
			b.setTypeIdAndData(44, (byte) 6, false);//STEP

		else if (b.getTypeId() == 128)//SANDSTONE_STAIRS
			b.setTypeIdAndData(44, (byte) 1, false);//STEP

		else if (b.getTypeId() == 134)//SPRUCE_WOOD_STAIRS
			b.setTypeIdAndData(126, (byte) 1, false);//WOOD_STEP

		else if (b.getTypeId() == 135)//BIRCH_WOOD_STAIRS
			b.setTypeIdAndData(126, (byte) 2, false);//WOOD_STEP

		else if (b.getTypeId() == 136)//JUNGLE_WOOD_STAIRS
			b.setTypeIdAndData(126, (byte) 3, false);//WOOD_STEP

		newBlock = b.getState();
	}




	public class revertBlock implements Runnable{
		Block b;
		revertBlock(Block block){
			b = block;
		}
		public void run() {
			b.setTypeIdAndData(originalBlock.getTypeId(), originalBlock.getData().getData(), false);
		}
	}
	public void revertBlockPause(Block b){
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new revertBlock(b), 20L);
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



			if (is.getType().equals(Material.WRITTEN_BOOK) && clicked.getType().equals(Material.WORKBENCH)){

				Block blockUp = clicked.getRelative(BlockFace.UP);
				if (BookSuiteBlockCheck.isInvertedStairs(blockUp) && !denyUseage(p)){

					BookMeta bm = (BookMeta) is.getItemMeta();
					if (checkPermission(p, bm.getAuthor()) && canObtainBook(p))
						operatePress(p, is, blockUp);
					event.setCancelled(true);
				}
			}
			
			
			
			//this is for checking mail
			if (clicked.getType().equals(Material.CHEST)){
				//test if there is a sign above it saying "mail"
				//if so, read from the player's local-file inventory and put those items into the chest
				//make sure to test if the player has the ability to look into the chest before adding items
				//once the items have been added to the players mailbox, REMOVE THEM FROM THE FILE
			}
		}



		if (event.getAction().equals(Action.RIGHT_CLICK_AIR)){
			Player p = event.getPlayer();
			if (p.getItemInHand().getType().equals(Material.WRITTEN_BOOK))
				if (p.hasPermission("booksuite.mail.send")){
					BookMeta bm = (BookMeta) p.getItemInHand().getItemMeta();
					new BookSuiteMailHandler(this, p).sendMail(bm);
					event.setCancelled(true);
				}
		}


	}
}
