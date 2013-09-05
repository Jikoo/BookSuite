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

import java.util.ArrayList;

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

public class MainListener implements Listener {

	BookSuite plugin = BookSuite.getInstance();

	private static MainListener instance;

	public static MainListener getInstance() {
		if (instance == null)
			instance = new MainListener();
		return instance;
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

		Player p = event.getPlayer();

		// right click action
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

			// get information about the click
			ItemStack is = p.getItemInHand();
			Block clicked = event.getClickedBlock();

			// if clicking a workbench, check to see if it is a press and act
			// accordingly
			if (plugin.functions.isPrintingPress(clicked)) {
				PrintingPress press = new PrintingPress(plugin,
						p.getName(), clicked);

				if (!p.hasPermission("booksuite.denynowarn.press")) {
					if (is.getType().equals(Material.MAP)) {
						if (plugin.functions.canObtainMap(p)) {
							press.operatePress();
							plugin.functions.copy(p);
							p.sendMessage(ChatColor.DARK_GREEN
									+ "Copied successfully!");
						}
						event.setCancelled(true);
					} else if (is.getType().equals(Material.WRITTEN_BOOK)) {
						BookMeta bm = (BookMeta) is.getItemMeta();

						if (plugin.functions.checkCopyPermission(p,
								bm.getAuthor())
								&& plugin.functions.canObtainBook(p)) {
							press.operatePress();
							plugin.functions.copy(p);
							p.sendMessage(ChatColor.DARK_GREEN
									+ "Copied successfully!");
						}
						event.setCancelled(true);
					} else if (is.getType().equals(Material.BOOK_AND_QUILL)) {
						if (p.hasPermission("booksuite.copy.unsigned")) {
							if (plugin.functions.canObtainBook(p)) {
								press.operatePress();
								plugin.functions.copy(p);
								p.sendMessage(ChatColor.DARK_GREEN
										+ "Copied successfully!");
							}
						} else {
							p.sendMessage(ChatColor.DARK_RED
									+ "You do not have permission to copy unsigned books!");
						}
						event.setCancelled(true);
					} else if (!(is.hasItemMeta() || is.getItemMeta() != null)) {
						return;
					}
				}
			} else if (plugin.functions.canMakePress(clicked, event.getBlockFace(), is, p)) {
				clicked.getRelative(BlockFace.UP).setTypeIdAndData(is.getTypeId(),
						plugin.functions.getCorrectStairOrientation(p), true);
				if (is.getAmount() == 1) {
					p.setItemInHand(null);
				} else {
					is.setAmount(is.getAmount() - 1);
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
					} else if (bm.getAuthor().equalsIgnoreCase(p.getName())) {
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
			} else if (plugin.functions.isMailBox(clicked)) {
				p.openInventory(plugin.mail.getMailBoxInv(p, plugin
						.getDataFolder().getPath()));
				event.setCancelled(true);
			}
			// else if (pluggins.functions.isLibrary(clicked, p)){}
		}

		// this is for taking a "package/envelope" that contains a "gift" and
		// opening it into your inventory.
		// meh ill clean it up later.
		else if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			if (p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)) {
				if (!(p.getItemInHand().hasItemMeta() || p.getItemInHand()
						.getItemMeta() != null))
					return;
				BookMeta bm = (BookMeta) p.getItemInHand().getItemMeta();
				if (bm.getTitle().contains("Package: ")) {
					if (plugin.mail.loadMail(p, bm, plugin.getDataFolder()
							.getPath()))
						event.setCancelled(true);
				} else if (p.hasPermission("booksuite.mail.send")
						&& bm.getTitle().equalsIgnoreCase("package"))
					if (plugin.mail.sendMail(p, bm, plugin.getDataFolder()
							.getPath()))
						event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if (event.getInventory().getTitle().contains("'s MailBox")) {
			plugin.mail.WriteMailContents(event.getInventory());
		}
	}

	@EventHandler
	public void onBookEdit(PlayerEditBookEvent event) {
		BookMeta obm = event.getPreviousBookMeta();
		BookMeta bm = event.getNewBookMeta();
		if (!event.getPlayer().hasPermission("booksuite.edit.other")
				&& obm.hasAuthor()
				&& obm.getAuthor() != null
				&& !plugin.alias.getAliases(event.getPlayer()).contains(
						obm.getAuthor())) {
			event.getPlayer().sendMessage(
					ChatColor.DARK_RED + "You'll need " + obm.getAuthor()
							+ ChatColor.DARK_RED
							+ "'s permission to edit this book!");
			event.setCancelled(true);
			return;
		} else {
			// additional authors, TODO
		}

		String alias = plugin.alias.getActiveAlias(event.getPlayer());
		if (event.isSigning()) {
			bm.setAuthor(alias);
			if (bm.hasLore()
					&& bm.getLore().contains(ChatColor.GRAY + "by " + alias)) {
				ArrayList<String> lore = (ArrayList<String>) bm.getLore();
				lore.remove(0);
				if (lore.isEmpty()) {
					lore = null;
				}
				bm.setLore(lore);
			}
		} else if (event.getPlayer().hasPermission("booksuite.alias.lock.auto")) {
			bm.setAuthor(alias);
			ArrayList<String> lore = new ArrayList<String>();
			if (bm.hasLore()) {
				if (!bm.getLore().contains(ChatColor.GRAY + "by " + alias)) {
					lore.add(ChatColor.GRAY + "by " + alias);
					lore.addAll(bm.getLore());
					bm.setLore(lore);
				}
			} else {
				lore.add(ChatColor.GRAY + "by " + alias);
				bm.setLore(lore);
			}
		}
		event.setNewBookMeta(bm);
	}

	public void disable() {
		HandlerList.unregisterAll(this);
	}
}
