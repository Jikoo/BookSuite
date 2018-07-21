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
package com.github.jikoo.booksuite;

import com.github.jikoo.booksuite.copy.PrintingPress;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scoreboard.Team;

public class MainListener implements Listener {

	private final BookSuite plugin;

	MainListener(BookSuite plugin) {
		this.plugin = plugin;
	}

	/**
	 * copy book (PrintingPress) or send mail
	 *
	 * @param event world triggered event
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {

		Player p = event.getPlayer();

		// If it isn't a right click, we don't care about it.
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		// Convenient variables, woo.
		ItemStack is = p.getInventory().getItemInMainHand();
		Block clicked = event.getClickedBlock();

		// PRESS OPERATION
		if (plugin.getConfig().getBoolean("enable-printing-presses")
				&& (clicked.getType() == Material.CRAFTING_TABLE
				&& plugin.getFunctions().isInvertedStairs(clicked.getRelative(BlockFace.UP))
				|| plugin.getFunctions().isInvertedStairs(clicked)
				&& clicked.getRelative(BlockFace.DOWN).getType() == Material.CRAFTING_TABLE)) {

			if (p.hasPermission("booksuite.denynowarn.press")) {
				return;
			}

			if (!p.hasPermission("booksuite.copy.self")) {
				p.sendMessage(plugin.getMessages().get("FAILURE_PERMISSION_COPY"));
				event.setCancelled(true);
				return;
			}

			if (is.getType() == Material.MAP && plugin.getFunctions().canObtainMap(p)) {
				new PrintingPress(plugin, clicked).operate();
				plugin.getFunctions().copy(p, 1);
				p.sendMessage(plugin.getMessages().get("SUCCESS_COPY"));
				event.setCancelled(true);
				return;
			}

			if (is.getType() == Material.WRITTEN_BOOK || is.getType() == Material.WRITABLE_BOOK) {
				BookMeta bm = (BookMeta) is.getItemMeta();

				if (plugin.getFunctions().checkCopyPermission(p, bm.getAuthor())
						&& plugin.getFunctions().canObtainBook(p)) {
					new PrintingPress(plugin, clicked).operate();
					plugin.getFunctions().copy(p, 1);
					p.sendMessage(plugin.getMessages().get("SUCCESS_COPY"));
				}
				event.setCancelled(true);
				return;
			}
			return;
		}

		// ERASER
		if ((is.getType() == Material.WRITTEN_BOOK
				|| is.getType() == Material.WRITABLE_BOOK)
				&& !plugin.getConfig().getBoolean("enable-erasers")
				&& clicked.getType() == Material.CAULDRON
				&& is.hasItemMeta()) {
			BookMeta bm = (BookMeta) is.getItemMeta();

			if (p.hasPermission("booksuite.denynowarn.erase")) {
				return;
			}
			if (!p.hasPermission("booksuite.block.erase")) {
				p.sendMessage(plugin.getMessages().get("FAILURE_PERMISSION_ERASE"));
				event.setCancelled(true);
				return;
			}

			BlockData data = clicked.getBlockData();
			if (!(data instanceof Levelled)) {
				return;
			}

			Levelled cauldron = (Levelled) data;

			if (p.getGameMode() != GameMode.CREATIVE && cauldron.getLevel() > 0 && !p.hasPermission("booksuite.block.erase.free")) {
				p.sendMessage(plugin.getMessages().get("FAILURE_ERASE_NOWATER"));
			} else if (plugin.getFunctions().isAuthor(p, bm.getAuthor()) || p.hasPermission("booksuite.block.erase.other")) {
				plugin.getFunctions().unsign(p);
				if (!p.hasPermission("booksuite.block.erase.free") && p.getGameMode() != GameMode.CREATIVE) {

					cauldron.setLevel(cauldron.getLevel() - 1);
					clicked.setBlockData(data, true);
				}
			} else {
				p.sendMessage(plugin.getMessages().get("FAILURE_PERMISSION_ERASE_OTHER"));
			}
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {

		if (event.getBlockAgainst().getType() != Material.CRAFTING_TABLE
				|| !event.getPlayer().hasPermission("booksuite.copy.createpress")
				|| !event.getBlockAgainst().getLocation().equals(event.getBlock().getLocation().add(0, -1, 0))
				|| !event.getBlock().getType().name().contains("STAIRS")) {
			return;
		}

		// EASY PRESS CREATION
		final Block block = event.getBlock();
		final BlockState state = event.getBlock().getState();
		Bukkit.getScheduler().runTask(this.plugin, new Runnable() {
			@Override
			public void run() {

				if (!block.getState().equals(state)) {
					// Ensure block has not been changed - event cancellation, etc.
					return;
				}
				BlockData data = block.getBlockData();
				if (!(data instanceof Bisected)) {
					return;
				}
				((Bisected) data).setHalf(Bisected.Half.TOP);
				block.setBlockData(data, true);
			}
		});

	}

	@EventHandler(ignoreCancelled = true)
	public void onBookEdit(PlayerEditBookEvent event) {
		Player player = event.getPlayer();

		BookMeta bm = event.getNewBookMeta();

		if (event.isSigning() && plugin.getConfig().getBoolean("enable-aliases") && player.hasPermission("booksuite.sign.alias")) {
			Team team = event.getPlayer().getScoreboard().getTeam(player.getName());
			StringBuilder name = new StringBuilder();
			if (team != null) {
				name.append(team.getPrefix());
			}
			if (team != null) {
				name.append(team.getSuffix());
			}
			bm.setAuthor(name.toString());
			if (event.getPlayer().hasPermission("booksuite.sign.color") && bm.hasTitle()) {
				bm.setTitle(plugin.getFunctions().addColor(bm.getTitle()));
			}
		}

		event.setNewBookMeta(bm);
	}
}
