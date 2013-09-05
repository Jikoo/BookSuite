/*******************************************************************************
 * Copyright (c) 2013 Ted Meyer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn - plugin surrounding libraries
 ******************************************************************************/
package com.github.Jikoo.BookSuite.mail;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class MailBox {
	private static Map<String, MailBox> boxes = new HashMap<String, MailBox>();

	public static MailBox getMailBox(Player p) {
		return MailBox.getMailBox(p.getName());
	}

	public static MailBox getMailBox(String p) {
		if (boxes.get(p) == null) {
			boxes.put(p, new MailBox(p, 5));
		}
		return boxes.get(p);
	}

	private String user;
	private int maxSize;
	private List<BookMailWrapper> inventory = new LinkedList<BookMailWrapper>();

	public MailBox(String user, int size) {
		this.user = user;
		this.maxSize = size;
	}

	public void sendMail() {

	}

	public void getMail() {
		if (this.inventory.size() < this.maxSize) {
			PostalService.getInstance().distribute(user,
					this.maxSize - this.inventory.size());
		}
	}

	public Inventory open(Player p) {
		Inventory mailbox = Bukkit.createInventory(p, maxSize, p.getName()
				+ "'s MailBox");
		getMail();

		for (BookMailWrapper bwm : this.inventory) {
			ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
			is.setItemMeta(bwm.getAllMeta());
			mailbox.addItem(is);
		}

		return mailbox;
	}

}
