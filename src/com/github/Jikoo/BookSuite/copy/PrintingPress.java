/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn- initial API and implementation
 *     Ted Meyer - some help
 ******************************************************************************/
package com.github.Jikoo.BookSuite.copy;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import com.github.Jikoo.BookSuite.BookSuite;


public class PrintingPress {
	
	BookSuite plugin;
	Block blockUp;
	BlockState originalBlock;
	BlockState changedBlock;
	String pName;
	
	
	public PrintingPress (BookSuite plugin, String pName, Block blockUp) {
		this.plugin = plugin;
		this.blockUp = blockUp;
		originalBlock = blockUp.getState();
		this.pName = pName;
	}




	public void operatePress() {
		changeStairBlock(blockUp);
		revertBlockPause(blockUp);
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

		else if (b.getTypeId() == 156)//QUARTZ_STAIRS
			b.setTypeIdAndData(44, (byte) 7, false);//STEP
		
		changedBlock = b.getState();
	}


	public void revertBlockPause(Block b) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new revertBlock(b), 20L);
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
				Player owner = Bukkit.getPlayerExact(pName);
				if (owner != null)
					owner.sendMessage(ChatColor.DARK_RED + "The press you used appears to have broken.");
			}
		}
	}
}
