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
import org.bukkit.material.Cauldron;

import com.github.Jikoo.BookSuite.copy.PrintingPress;
import com.github.Jikoo.BookSuite.mail.MailBox;

public class MainListener implements Listener {

	BookSuite plugin = BookSuite.getInstance();

	private static MainListener instance;

	protected static MainListener getInstance() {
		if (instance == null)
			instance = new MainListener();
		return instance;
	}

	/**
	 * copy book (PrintingPress) or send mail
	 * 
	 * @param event world triggered event
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}

		Player p = event.getPlayer();

		// right click action
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		// get information about the click
		ItemStack is = p.getItemInHand();
		Block clicked = event.getClickedBlock();

		// if clicking a workbench, check to see if it is a press and act
		// accordingly
		if (plugin.functions.isPrintingPress(clicked)) {
			PrintingPress press = new PrintingPress(plugin, p.getName(), clicked);

			if (p.hasPermission("booksuite.denynowarn.press")) {
				return;
			}

			if (!p.hasPermission("booksuite.copy.self")) {
				p.sendMessage(plugin.msgs.get("FAILURE_PERMISSION_COPY"));
				event.setCancelled(true);
				return;
			}

			if (is.getType() == Material.MAP && plugin.functions.canObtainMap(p)) {
				press.operatePress();
				plugin.functions.copy(p, 1);
				p.sendMessage(plugin.msgs.get("SUCCESS_COPY"));
				event.setCancelled(true);
				return;
			}

			if (is.getType().equals(Material.WRITTEN_BOOK) || is.getType().equals(Material.BOOK_AND_QUILL)) {
				BookMeta bm = (BookMeta) is.getItemMeta();

				if (plugin.functions.checkCopyPermission(p, bm.getAuthor())
						&& plugin.functions.canObtainBook(p)) {
					press.operatePress();
					plugin.functions.copy(p, 1);
					p.sendMessage(plugin.msgs.get("SUCCESS_COPY"));
				}
				event.setCancelled(true);
				return;
			}
		} else if (plugin.functions.canMakePress(clicked, event.getBlockFace(), is, p)) {
			clicked.getRelative(BlockFace.UP).setTypeIdAndData(is.getTypeId(),
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
			return;
		}

		// ERASER
		if (plugin.functions.canErase(clicked, is)) {
			BookMeta bm = (BookMeta) is.getItemMeta();

			if (p.hasPermission("booksuite.denynowarn.erase")) {
				return;
			}
			if (!p.hasPermission("booksuite.block.erase")) {
				p.sendMessage(plugin.msgs.get("FAILURE_PERMISSION_ERASE"));
				event.setCancelled(true);
				return;
			}

			Cauldron cauldron = (Cauldron) clicked.getState().getData();
			if (p.getGameMode() != GameMode.CREATIVE && cauldron.isEmpty()
					&& !p.hasPermission("booksuite.block.erase.free")) {
				p.sendMessage(plugin.msgs.get("FAILURE_ERASE_NOWATER"));
			} else if (plugin.functions.isAuthor(p, bm.getAuthor())
					|| p.hasPermission("booksuite.block.erase.other")) {
				plugin.functions.unsign(p);
				if (!p.hasPermission("booksuite.block.erase.free")
						|| p.getGameMode() != GameMode.CREATIVE) {
					clicked.setData((byte) (clicked.getData() - 1));
					// future ref in case Bukkit makes a non-deprecated setter for water:
					// Integer.parseInt(cauldron.toString().replace("/3 FULL", "").replace("FULL", "3").replace("EMPTY").replace(" CAULDRON"));
				}
			} else {
				p.sendMessage(plugin.msgs.get("FAILURE_PERMISSION_ERASE_OTHER"));
			}
			event.setCancelled(true);
			return;
		}

		// MAIL
		if (plugin.functions.isMailBox(clicked)) {
			p.openInventory(MailBox.getMailBox(p).open(p));
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getInventory().getTitle().contains("'s MailBox")) {
			MailBox.getMailBox(event.getPlayer().getName()).sendMail(event.getInventory());
		}
	}

	/**
	 * When a player closes a book, this event is fired. BookSuite introduces
	 * several literary protection features - all players who work on a book are
	 * credited in order of edit.
	 * 
	 * @param event the PlayerBookEditEvent
	 */
	@EventHandler
	public void onBookEdit(PlayerEditBookEvent event) {
		if (event.isCancelled()) {
			return;
		}

		BookMeta obm = event.getPreviousBookMeta();
		BookMeta bm = event.getNewBookMeta();

		// TODO this sucks for the editor, all work lost. Check on open, not on finish
		if (!event.getPlayer().hasPermission("booksuite.edit.other") && obm.hasAuthor()
				&& obm.getAuthor() != null) {
			event.setCancelled(true);
			for (String author : plugin.functions.parseAuthors(obm.getAuthor())) {
				if (plugin.alias.getAliases(event.getPlayer()).contains(author)) {
					event.setCancelled(false);
				}
			}
			if (event.isCancelled()) {
				event.getPlayer().sendMessage(plugin.msgs.get("FAILURE_PERMISSION_ALIAS")
						.replace("<author(s)>", obm.getAuthor().replace(" and ", " or ")));
				return;
			}
		}
		if (event.isSigning() || event.getPlayer().hasPermission("booksuite.sign.alias")) {
			bm = plugin.functions.addAuthor(bm, obm.hasAuthor() ? obm.getAuthor() : null,
					event.getPlayer(), event.isSigning());
			if (event.getPlayer().hasPermission("booksuite.sign.color") && bm.hasTitle()) {
				bm.setTitle(Functions.getInstance().addColor(bm.getTitle()));
			}
		}
		event.setNewBookMeta(bm);
	}

	public void disable() {
		HandlerList.unregisterAll(this);
	}
}
