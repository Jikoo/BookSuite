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
package com.github.Jikoo.BookSuite;

import java.io.File;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class MailExecutor {// TODO keep meaning to completely redo this all in
							// favor of mailboxes/postboxes
	private Functions functions = Functions.getInstance();
	private FileManager filemanager = FileManager.getInstance();
	private static MailExecutor instance;

	public boolean sendMail(Player p, BookMeta bm, String pluginDataFolder) {
		bm = (BookMeta) p.getItemInHand().getItemMeta();
		if (!p.getName().equals(bm.getAuthor())
				&& !p.hasPermission("booksuite.mail.send.other")) {
			p.sendMessage(ChatColor.DARK_RED + p.getName()
					+ ", you shouldn't be trying to send letters for "
					+ bm.getAuthor() + "...");
		} else {
			boolean mailHasItemAttached = false;
			String[] sendingData = parseSendingData(bm.getPage(1));

			BookMeta newBook = (BookMeta) new ItemStack(Material.WRITTEN_BOOK)
					.getItemMeta();
			newBook.setAuthor(bm.getAuthor());
			Inventory inv = p.getInventory();
			ItemStack removeThis = new ItemStack(Material.DIRT, 1);
			boolean playerHasItem = false;
			for (int i = 2; i <= bm.getPageCount(); i++)
				newBook.addPage(bm.getPage(i));
			if (sendingData[2] != null && sendingData[2] != "") {
				mailHasItemAttached = true;
				newBook.setTitle("Package: " + sendingData[0]);
				newBook.addPage("To: " + sendingData[1] + "\nAttached:\n"
						+ sendingData[2]);
				for (ItemStack is : inv.getContents())
					if (is != null)
						if (is.hasItemMeta())
							if (is.getItemMeta().hasDisplayName())
								if (is.getItemMeta().getDisplayName()
										.equalsIgnoreCase(sendingData[2])) {
									removeThis = is;
									playerHasItem = true;
									break;
								}
			} else
				newBook.setTitle(sendingData[0]);

			if (mailHasItemAttached && !playerHasItem) {
				p.sendMessage(ChatColor.DARK_RED
						+ "Error: no such named item, please check spelling.");
				functions.unsign(p);
				return false;
			}

			if (filemanager.appendMailIndex(pluginDataFolder + "/Mail/"
					+ sendingData[1] + "/", sendingData[0])) {
				if (new File(pluginDataFolder + "/Mail/" + sendingData[1]
						+ "/Books/", sendingData[0]).exists()) {
					if (mailHasItemAttached && playerHasItem) {
						if (filemanager.makeFileFromItemStack(removeThis,
								pluginDataFolder + "/Mail/" + sendingData[1]
										+ "/Items/", sendingData[2])) {
							inv.remove(removeThis);
						} else {
							p.sendMessage(ChatColor.DARK_RED
									+ "Error: "
									+ sendingData[1]
									+ " already has an item by that name in their mailbox.");
							functions.unsign(p);
							return false;
						}
					}

					if (filemanager.makeFileFromBookMeta(newBook,
							pluginDataFolder + "/Mail/" + sendingData[1]
									+ "/Books/", sendingData[0])) {
						inv.remove(p.getItemInHand());
						p.sendMessage(ChatColor.DARK_GREEN
								+ "Mail sent successfully!");
						return true;
					}
				} else {
					p.sendMessage(ChatColor.DARK_RED
							+ "Error: "
							+ sendingData[1]
							+ " already has a book by that name in their mailbox.");
					functions.unsign(p);
				}
			} else
				p.sendMessage(ChatColor.DARK_RED + "Error writing mail index!");
		}
		return false;
	}

	public boolean loadMail(Player p, BookMeta bm, String pluginDataFolder) {
		bm = (BookMeta) p.getItemInHand().getItemMeta();
		String[] checks = bm.getPage(bm.getPageCount()).replace("To: ", "")
				.replace("Attached:\n", "").split("\n");
		if (p.getName() == checks[0]) {
			if (p.getInventory().firstEmpty() != -1) {
				bm.setTitle(bm.getTitle().replace("Package: ", ""));
				bm.setPage(bm.getPageCount(), "Attached:\n" + checks[1]);
				filemanager.makeItemStackFromFile(pluginDataFolder + "/Mail/"
						+ p.getName() + "/Items/", checks[1]);
				filemanager.delete(pluginDataFolder + "/Mail/" + p.getName()
						+ "/Items/", checks[1]);
				return true;
			}
			p.sendMessage(ChatColor.DARK_RED
					+ "You do not have space to unpack this book.");
		}
		return false;
	}

	public Inventory getMailBoxInv(Player p, String pluginDataFolder) {
		Inventory mailbox = Bukkit.createInventory(p, 2, p.getName()
				+ "'s MailBox");

		Scanner s;
		try {
			s = new Scanner(pluginDataFolder + "/Mail/" + p.getName()
					+ "/index.bsm");
			while (s.hasNext()) {
				if (mailbox.firstEmpty() != -1) {
					ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
					is.setItemMeta(filemanager.makeBookMetaFromText(p,
							s.nextLine() + ".book", pluginDataFolder + "/Mail/"
									+ p.getName() + "/Books/", false));
					mailbox.addItem(is);
				}

			}
			s.close();
		} catch (Exception e) {
			p.sendMessage(ChatColor.DARK_RED
					+ "You have not receved any mail yet.");
		}
		return mailbox;
	}

	public String[] parseSendingData(String firstPage) {
		String[] pageData = firstPage.split("\n");
		pageData[0] = pageData[0].replaceFirst(
				"\\A.*([Pp]ackage|[Tt]itle):\\s*", "").replaceAll("\\W", "");
		pageData[1] = pageData[1].replaceFirst("\\A.*[Tt]o:\\s*", "")
				.replaceAll("\\W", "");
		if (pageData[2] != null) {
			pageData[2] = pageData[2].replaceFirst(
					"\\A.*([Ii]tem|[Aa]ttach):\\s*", "");
			if (pageData[2].equalsIgnoreCase("n/a")
					|| pageData[2].equalsIgnoreCase("none")
					|| pageData[2].equalsIgnoreCase("nothing"))
				pageData[2] = "";
		}
		return pageData;
	}

	public String parseReceivingData(String lastpage) {
		String toItem = lastpage.replace("To: ", "").replace("Item: ", "");

		return toItem;
	}

	public void WriteMailContents(Inventory inventory) {
		// TODO Auto-generated method stub

	}

	private MailExecutor() {
	}

	public static MailExecutor getInstance() {
		if (instance == null)
			instance = new MailExecutor();
		return instance;
	}
}
