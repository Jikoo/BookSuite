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

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.material.Cauldron;
import org.bukkit.scoreboard.Team;

import com.github.jikoo.booksuite.copy.PrintingPress;

public class MainListener implements Listener {

	private final BookSuite plugin;

	protected MainListener(BookSuite plugin) {
		this.plugin = plugin;
	}

	/**
	 * copy book (PrintingPress) or send mail
	 * 
	 * @param event world triggered event
	 */
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {

		Player p = event.getPlayer();

		// If it isn't a right click, we don't care about it.
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		// Convenient variables, woo.
		ItemStack is = p.getItemInHand();
		Block clicked = event.getClickedBlock();

		// PRESS OPERATION
		if (plugin.getFunctions().isPrintingPress(clicked)) {

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

			if (is.getType() == Material.WRITTEN_BOOK || is.getType() == Material.BOOK_AND_QUILL) {
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

		// PRESS EASY CREATION
		if (plugin.getFunctions().canMakePress(clicked, event.getBlockFace(), is, p)) {
			clicked.getRelative(BlockFace.UP).setTypeIdAndData(is.getTypeId(), plugin.getFunctions().getCorrectStairOrientation(p), true);
			if (p.getGameMode() != GameMode.CREATIVE) {
				if (is.getAmount() == 1) {
					p.setItemInHand(null);
				} else {
					is.setAmount(is.getAmount() - 1);
				}
			}
			event.setCancelled(true);
			p.updateInventory();
			return;
		}

		// ERASER
		if (plugin.getFunctions().canErase(clicked, is)) {
			BookMeta bm = (BookMeta) is.getItemMeta();

			if (p.hasPermission("booksuite.denynowarn.erase")) {
				return;
			}
			if (!p.hasPermission("booksuite.block.erase")) {
				p.sendMessage(plugin.getMessages().get("FAILURE_PERMISSION_ERASE"));
				event.setCancelled(true);
				return;
			}

			Cauldron cauldron = (Cauldron) clicked.getState().getData();
			if (p.getGameMode() != GameMode.CREATIVE && cauldron.isEmpty() && !p.hasPermission("booksuite.block.erase.free")) {
				p.sendMessage(plugin.getMessages().get("FAILURE_ERASE_NOWATER"));
			} else if (plugin.getFunctions().isAuthor(p, bm.getAuthor()) || p.hasPermission("booksuite.block.erase.other")) {
				plugin.getFunctions().unsign(p);
				if (!p.hasPermission("booksuite.block.erase.free") && p.getGameMode() != GameMode.CREATIVE) {
					clicked.setData((byte) (clicked.getData() - 1));
				}
			} else {
				p.sendMessage(plugin.getMessages().get("FAILURE_PERMISSION_ERASE_OTHER"));
			}
			event.setCancelled(true);
			return;
		}
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
