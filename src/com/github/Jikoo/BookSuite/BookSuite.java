package com.github.Jikoo.BookSuite;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
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
	String version = "2.1.0";
	String neededSupplies = "";
	BlockState originalBlock;
	BlockState newBlock;
	
	
	@Override
	public void onEnable() {
		/*saveDefaultConfig();
		try {
			//todo with adding perms true/false support. Pushing working version with "stamp" + bugfixes to GitHub, will do this tonight.
		} catch (Exception e1) {
		}*/
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("makebook").setExecutor(new BookSuiteCommandExecutor(this));
		getLogger().info("BookSuite v"+version+" enabled!");
	}
	@Override
	public void onDisable() {
		getLogger().info("BookSuite v"+version+" disabled!");
	}

	public boolean hasSupplies(Inventory inv){
		if (inv.contains(Material.BOOK) && inv.contains(Material.INK_SACK))
			return true;
		if (inv.contains(Material.BOOK) && !inv.contains(Material.INK_SACK))
			neededSupplies = "an ink sack";
		else if (!inv.contains(Material.BOOK) && inv.contains(Material.INK_SACK))
			neededSupplies = "a book";
		else if (!inv.contains(Material.BOOK) && !inv.contains(Material.INK_SACK))
			neededSupplies = "a book and an ink sack";
		return false;
	}
	
	
	public boolean canObtainBook(Player p){
		Inventory inv = p.getInventory();
		if (p.hasPermission("booksuite.free") || p.getGameMode().equals(GameMode.CREATIVE)){
			if (inv.firstEmpty() == -1){
				p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
				return false;
			}
			return true;
		}
		if (hasSupplies(inv)){
			if (inv.firstEmpty() == -1){
				inv.removeItem(new ItemStack(Material.INK_SACK, 1));
				inv.removeItem(new ItemStack(Material.BOOK, 1));
				return true;
			}
			else p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
			return false;
		}
		else{
			p.sendMessage(ChatColor.DARK_RED+"To copy a book, you need "+neededSupplies+".");
			return false;
		}
	}
	
	public void operatePress(Player p, ItemStack is, Block b){
		p.getInventory().addItem(is.clone());
		p.updateInventory();
		p.sendMessage(ChatColor.DARK_GREEN+"Book copied!");
		originalBlock = b.getState();
		changeStairBlock(b);
		revertBlockPause(b);
	}
	
	//do not warn player about lack of permission (default: false) Need config for this.
	/*	public boolean denyUseage(Player p){
		if (p.hasPermission("booksuite.copy.deny.nowarn") && !(p.hasPermission("booksuite.copy.self") || p.hasPermission("booksuite.copy.other")))
			return true;
		return false;
	}*/
	
	//check player permissions
	public boolean checkPermission(Player p, String a){
		if (p.hasPermission("booksuite.copy.other"))
			return true;
		if (a.equals(p.getName()) && p.hasPermission("booksuite.copy.self"))
			return true;
		/*if (p.hasPermission("booksuite.copy.deny")) //Need config for this.
						p.sendMessage(ChatColor.DARK_RED+"You do not have permission to copy books.");*/
		else  
			p.sendMessage(ChatColor.DARK_RED+"You do not have permission to copy others' books.");
		return false;
	}
	
	
	/* Change and store stairblock over the "press"
	 * Todo: change to arraylist? So much more elegant than this crap.
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
	
	
	
	public boolean isInvertedStairs(Block b){
		int mat = b.getTypeId();
		if((mat == 53) || (mat == 67) || (mat == 108) || (mat == 109) || (mat == 114) || (mat == 128) || (mat == 134)  || (mat == 135) || (mat == 136))
			if(b.getData()>3)
				return true;
		return false;
	}
	
	@EventHandler //copy book (PrintingPress)
	public void onPlayerInteract(PlayerInteractEvent event){
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			Player p = event.getPlayer();
			ItemStack is = p.getItemInHand();
			Block blockUp = event.getClickedBlock().getRelative(BlockFace.UP);
			if (is.getType().equals(Material.WRITTEN_BOOK) && event.getClickedBlock().getType().equals(Material.WORKBENCH))
				if (isInvertedStairs(blockUp)){
					//block storageUnit = event.getClickedBlock.getRelative(stairFaceDir(blockUp.getData())); //returns BlockFace.<dir> where 4=north, 5 = south, 6 = east, 7 = west
					//if (!denyUseage(p)){
						BookMeta bm = (BookMeta) is.getItemMeta();
						if (checkPermission(p, bm.getAuthor()))
							if (canObtainBook(p))
								operatePress(p, is, blockUp);
						event.setCancelled(true);
					//}
				}
		}
	}
}
