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

	private final BookSuite plugin;
	private final BlockState originalBlock;
	private final BlockState changedBlock;

	public PrintingPress(BookSuite plugin, Block block) {
		this.plugin = plugin;
		if (!plugin.getFunctions().isInvertedStairs(block)) {
			block = block.getRelative(BlockFace.UP);
		}
		originalBlock = block.getState();
		changedBlock = getStairSlabState(block);
	}

	/**
	 * Turns the stair block into a slab for graphical effect.
	 * 
	 * @param b the stair block to be transformed
	 */
	@SuppressWarnings("deprecation") // No alternative to this yet.
	private BlockState getStairSlabState(Block block) {
		BlockState state = block.getState();
		if (state.getType() == Material.SANDSTONE_STAIRS) {
			state.setType(Material.STEP);
			state.setRawData((byte) 1);
		} else if (state.getType() == Material.COBBLESTONE_STAIRS) {
			state.setType(Material.STEP);
			state.setRawData((byte) 3);
		} else if (state.getType() == Material.BRICK_STAIRS) {
			state.setType(Material.STEP);
			state.setRawData((byte) 4);
		} else if (state.getType() == Material.SMOOTH_STAIRS) {
			state.setType(Material.STEP);
			state.setRawData((byte) 5);
		} else if (state.getType() == Material.NETHER_BRICK_STAIRS) {
			state.setType(Material.STEP);
			state.setRawData((byte) 6);
		} else if (state.getType() == Material.QUARTZ_STAIRS) {
			state.setType(Material.STEP);
			state.setRawData((byte) 7);
		} else if (state.getType() == Material.WOOD_STAIRS) {
			state.setType(Material.WOOD_STEP);
			state.setRawData((byte) 0);
		} else if (state.getType() == Material.SPRUCE_WOOD_STAIRS) {
			state.setType(Material.WOOD_STEP);
			state.setRawData((byte) 1);
		} else if (state.getType() == Material.BIRCH_WOOD_STAIRS) {
			state.setType(Material.WOOD_STEP);
			state.setRawData((byte) 2);
		} else if (state.getType() == Material.JUNGLE_WOOD_STAIRS) {
			state.setType(Material.WOOD_STEP);
			state.setRawData((byte) 3);
		} else if (state.getType() == Material.ACACIA_STAIRS) {
			state.setType(Material.WOOD_STEP);
			state.setRawData((byte) 4);
		} else if (state.getType() == Material.DARK_OAK_STAIRS) {
			state.setType(Material.WOOD_STEP);
			state.setRawData((byte) 5);
		} else state.setType(Material.STEP);

		return state;
	}

	public void operate() {
		if (changedBlock.getData().equals(originalBlock.getData())) {
			// Unknown stair type
			return;
		}
		changedBlock.update(true);
		new BukkitRunnable() {
			@Override
			public void run() {
				if (changedBlock.getType() == changedBlock.getBlock().getType()) {
					originalBlock.update(true);
				}
			}
		}.runTaskLater(plugin, 20L);
	}
}
