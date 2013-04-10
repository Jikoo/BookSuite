package com.github.Jikoo.BookSuite;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class PrintingPress {
	
	BookSuite plugin;
	Player p;
	ItemStack is;
	Block blockUp;
	BlockState originalBlock;
	BlockState changedBlock;
	
	
	public PrintingPress (BookSuite plugin, Player p, ItemStack is, Block blockUp) {
		this.plugin = plugin;
		this.p = p;
		this.is = is;
		this.blockUp = blockUp;
		originalBlock = blockUp.getState();
	}




	/**
	 * @param a
	 * @return
	 */
	public boolean checkCopyPermission(String a) {
		if (p.hasPermission("booksuite.copy.other"))
			return true;
		if (p.hasPermission("booksuite.copy.self") && a.equals(p.getName()))
			return true;
		else if (p.hasPermission("booksuite.copy.self"))
			p.sendMessage(ChatColor.DARK_RED+"You do not have permission to copy others' books.");
		else
			p.sendMessage(ChatColor.DARK_RED+"You do not have permission to copy books.");
		return false;
	}




	public boolean denyUseage() {
		if (p.hasPermission("booksuite.denynowarn.press"))
			return true;
		return false;
	}




	public void operatePress() {
		changeStairBlock(blockUp);
		revertBlockPause(blockUp);
		ItemStack duplicate = is.clone();
		duplicate.setAmount(1);
		p.getInventory().addItem(duplicate);
		p.updateInventory();
		p.sendMessage(ChatColor.DARK_GREEN+"Copied successfully!");
	}




	/**
	 * turns the stair block into a slab for graphical effect
	 * 
	 * @param b the stair block to be transformed
	 */
	public void changeStairBlock(Block b) {
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

		else if (b.getTypeId() == 156)//Quartz stairs
			b.setTypeIdAndData(44, (byte) 7, false);//STEP
		
		changedBlock = b.getState();
	}


	public class revertBlock implements Runnable {
		Block b;
		revertBlock(Block block) {
			b = block;
		}
		public void run() {
			if(b.getType().equals(changedBlock.getType())) {
				b.setTypeIdAndData(originalBlock.getTypeId(), originalBlock.getData().getData(), false);
			} else {
				Player[] plist = Bukkit.getOnlinePlayers();
				for (int i = 0; i < plist.length; i++){
					if (plist[i].getName().equals(p.getName())) {
						plist[i].sendMessage(ChatColor.DARK_RED+"The press you used appears to have broken.");
						return;
					}
				}
			}
		}
	}
	public void revertBlockPause(Block b) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new revertBlock(b), 20L);
	}
}
