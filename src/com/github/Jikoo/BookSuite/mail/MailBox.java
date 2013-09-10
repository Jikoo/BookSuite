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

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class MailBox {
	private String user;
	private int maxSize;
	private List<BookMailWrapper> inventory = new LinkedList<BookMailWrapper>();

	public MailBox(String user, int size) {
		this.user = user;
		this.maxSize = size;
	}

	public void sendMail(Inventory y) {
		List<BookMailWrapper> inv = new LinkedList<BookMailWrapper>();
		for (ItemStack i : y) {
			if (i != null && i.getType().equals(Material.WRITTEN_BOOK)) {
				BookMeta b = (BookMeta) i.getItemMeta();
				boolean l = "letter".equalsIgnoreCase(b.getTitle());
				boolean p = "package".equalsIgnoreCase(b.getTitle());
				if (l || p) {
					PostalService.getInstance().collect(new BookMailWrapper(b));
				}
				else {
					inv.add(new BookMailWrapper(b));
				}
			}
		}
		this.inventory = inv;
	}

	public void getMail() {
		if (this.inventory.size() < this.maxSize) {
			this.inventory.addAll(PostalService.getInstance().distribute(user,
					this.maxSize - this.inventory.size()));
		}
	}

	public Inventory open(Player p) {
		Inventory mailbox = Bukkit.createInventory(p, maxSize, p.getName()
				+ "'s MailBox");
		getMail();

		if (p.getName().equals(this.user)) {
			for (BookMailWrapper bwm : this.inventory) {
				ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
				is.setItemMeta(bwm.getAllMeta());
				mailbox.addItem(is);
			}
		}

		return mailbox;
	}

}
