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
	 * @param block the stair block to be transformed
	 */
	private BlockState getStairSlabState(Block block) {
		BlockState state = block.getState();
		Material newMaterial = Material.getMaterial(block.getType().name().replace("STAIRS", "SLAB"));
		if (newMaterial == null) {
			newMaterial = Material.STONE_SLAB;
		}
		state.setType(newMaterial);
		return state;
	}

	public void operate() {
		changedBlock.update(true);
		new BukkitRunnable() {
			@Override
			public void run() {
				if (changedBlock.getType() == changedBlock.getBlock().getType()) {
					originalBlock.update(true);
				}
			}
		}.runTaskLater(plugin, 10L);
	}
}
