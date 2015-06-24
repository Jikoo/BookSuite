/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn - ideas and implementation
 *     Ted Meyer - IO assistance and BML (Book Markup Language)
 ******************************************************************************/
package com.github.jikoo.booksuite.copy;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.jikoo.booksuite.BookSuite;

public class PrintingPress {

	Block blockUp;
	BlockState originalBlock;
	BlockState changedBlock;

	public PrintingPress(Block block) {
		if (BookSuite.getInstance().functions.isInvertedStairs(block)) {
			this.blockUp = block;
		} else {
			this.blockUp = block.getRelative(BlockFace.UP);
		}
	}

	public void operatePress() {
		originalBlock = blockUp.getState();
		changedBlock = changeStairBlock(blockUp);
		revertBlockPause(blockUp);
	}

	/**
	 * turns the stair block into a slab for graphical effect
	 * 
	 * @param b the stair block to be transformed
	 */
	@SuppressWarnings("deprecation") // No alternative to this yet.
	private BlockState changeStairBlock(Block b) {
		if (b.getType() == Material.SANDSTONE_STAIRS) {
			b.setType(Material.STEP);
			b.setData((byte) 1);
		} else if (b.getType() == Material.COBBLESTONE_STAIRS) {
			b.setType(Material.STEP);
			b.setData((byte) 3);
		} else if (b.getType() == Material.BRICK_STAIRS) {
			b.setType(Material.STEP);
			b.setData((byte) 4);
		} else if (b.getType() == Material.SMOOTH_STAIRS) {
			b.setType(Material.STEP);
			b.setData((byte) 5);
		} else if (b.getType() == Material.NETHER_BRICK_STAIRS) {
			b.setType(Material.STEP);
			b.setData((byte) 6);
		} else if (b.getType() == Material.QUARTZ_STAIRS) {
			b.setType(Material.STEP);
			b.setData((byte) 7);
		} else if (b.getType() == Material.WOOD_STAIRS) {
			b.setType(Material.WOOD_STEP);
			b.setData((byte) 0);
		} else if (b.getType() == Material.SPRUCE_WOOD_STAIRS) {
			b.setType(Material.WOOD_STEP);
			b.setData((byte) 1);
		} else if (b.getType() == Material.BIRCH_WOOD_STAIRS) {
			b.setType(Material.WOOD_STEP);
			b.setData((byte) 2);
		} else if (b.getType() == Material.JUNGLE_WOOD_STAIRS) {
			b.setType(Material.WOOD_STEP);
			b.setData((byte) 3);
		} else if (b.getType() == Material.ACACIA_STAIRS) {
			b.setType(Material.WOOD_STEP);
			b.setData((byte) 4);
		} else if (b.getType() == Material.DARK_OAK_STAIRS) {
			b.setType(Material.WOOD_STEP);
			b.setData((byte) 5);
		} else b.setType(Material.STEP);

		return b.getState();
	}

	private void revertBlockPause(final Block b) {
		new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				if (b.getType() == changedBlock.getType()) {
					b.setTypeIdAndData(originalBlock.getTypeId(), originalBlock.getData().getData(), false);
				}
			}
		}.runTaskLater(BookSuite.getInstance(), 20L);
	}
}
