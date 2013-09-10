/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn - ideas and implementation
 *     Ted Meyer - IO assistance and BML (Book Markup Language) plus code cleanliness =P
 ******************************************************************************/
package com.github.Jikoo.BookSuite;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.github.Jikoo.BookSuite.copy.PrintingPress;
import com.github.Jikoo.BookSuite.mail.MailBox;
import com.github.Jikoo.BookSuite.module.BookSuiteModule;
import com.github.Jikoo.BookSuite.module.ReflectiveModuleInstantiatier;

public class MainListener implements Listener {

	// singleton instance
	private static MainListener instance;

	protected static MainListener getInstance() {
		if (instance == null) {
			instance = new MainListener();
			ReflectiveModuleInstantiatier.loadModules(instance);
		}
		return instance;
	}

	// access to the plugin main class
	BookSuite plugin = BookSuite.getInstance();

	// list of the modules
	private List<BookSuiteModule> modules = new LinkedList<BookSuiteModule>();

	/**
	 * 
	 * @param m
	 *            the module that is to be added
	 */
	public void addModule(BookSuiteModule m) {
		this.modules.add(m);
	}

	/**
	 * copy book (PrintingPress) or send mail
	 * 
	 * @param event
	 *            world triggered event
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}

		for (BookSuiteModule bsm : modules) {
			if (bsm.isEnabled() && bsm.isTriggeredByEvent(event)) {
				event.setCancelled(true);
			}
			if (event.isCancelled()) {
				return;
			}
		}

		// new
		//
		// old

		Player p = event.getPlayer();

		// right click action
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

			// get information about the click
			ItemStack is = p.getItemInHand();
			Block clicked = event.getClickedBlock();

			if (plugin.functions.canMakePress(clicked,
					event.getBlockFace(), is, p)) {
				clicked.getRelative(BlockFace.UP).setTypeIdAndData(
						is.getTypeId(),
						plugin.functions.getCorrectStairOrientation(p), true);
				if (p.getGameMode() != GameMode.CREATIVE) {
					if (is.getAmount() == 1) {
						p.setItemInHand(null);
					} else {
						is.setAmount(is.getAmount() - 1);
					}
				}
				event.setCancelled(true);
				p.updateInventory();
			} else if (plugin.functions.canErase(clicked, is)) {
				BookMeta bm = (BookMeta) is.getItemMeta();

				if (p.hasPermission("booksuite.block.erase")) {
					if (clicked.getData() < 1
							&& !p.getGameMode().equals(GameMode.CREATIVE)
							&& !p.hasPermission("booksuite.block.erase.free")) {
						p.sendMessage(ChatColor.DARK_RED
								+ "You'll need some water to unsign this book.");
					} else if (plugin.functions.isAuthor(p, bm.getAuthor())) {
						plugin.functions.unsign(p);
						if (!p.hasPermission("booksuite.block.erase.free")
								&& !p.getGameMode().equals(GameMode.CREATIVE)) {
							clicked.setData((byte) (clicked.getData() - 1));
						}
					} else if (p.hasPermission("booksuite.block.erase.other")) {
						plugin.functions.unsign(p);
						if (!p.hasPermission("booksuite.block.erase.free")
								&& !p.getGameMode().equals(GameMode.CREATIVE)) {
							clicked.setData((byte) (clicked.getData() - 1));
						}

					} else {
						p.sendMessage(ChatColor.DARK_RED
								+ "You can only unsign your own books.");
					}
					event.setCancelled(true);

				} else if (!p.hasPermission("booksuite.denynowarn.erase")) {
					p.sendMessage(ChatColor.DARK_RED
							+ "You do not have permission to use erasers.");
					event.setCancelled(true);
				}
			}
			/*
			 * else if (plugin.functions.isLibrary(clicked, p)){
			 * 
			 * }
			 */
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		for (BookSuiteModule bsm : modules) {
			if (bsm.isTriggeredByEvent(event)) {
				// meh
			}
		}
	}

	/**
	 * When a player closes a book, this event is fired. BookSuite introduces
	 * several literary protection features - all players who work on a book are
	 * credited in order of edit.
	 * 
	 * @param event
	 *            the PlayerBookEditEvent
	 */
	@EventHandler
	public void onBookEdit(PlayerEditBookEvent event) {
		if (event.isCancelled()) {
			return;
		}

		BookMeta obm = event.getPreviousBookMeta();
		BookMeta bm = event.getNewBookMeta();

		if (!event.getPlayer().hasPermission("booksuite.edit.other")
				&& obm.hasAuthor() && obm.getAuthor() != null) {
			event.setCancelled(true);
			for (String author : plugin.functions.parseAuthors(obm.getAuthor())) {
				if (plugin.alias.getAliases(event.getPlayer()).contains(author)) {
					event.setCancelled(false);
				}
			}
			if (event.isCancelled()) {
				event.getPlayer().sendMessage(
						ChatColor.DARK_RED + "You'll need "
								+ obm.getAuthor().replace(" and ", " or ")
								+ ChatColor.DARK_RED
								+ "'s permission to edit this book!");
				return;
			}
		}
		if (event.isSigning()
				|| event.getPlayer().hasPermission("booksuite.alias.sign")) {
			bm = plugin.functions.addAuthor(bm,
					obm.hasAuthor() ? obm.getAuthor() : null,
					event.getPlayer(), event.isSigning());
		}
		event.setNewBookMeta(bm);
	}

	public void disable() {
		HandlerList.unregisterAll(this);
	}
}
