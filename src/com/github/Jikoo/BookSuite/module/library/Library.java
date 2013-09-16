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

package com.github.Jikoo.BookSuite.module.library;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class Library {

	Map<Player, List<Book>> checkouts = new HashMap<Player, List<Book>>();
	private List<Voxel> shelves;
	private final int ID;

	public Library(int ID, List<Voxel> shelves) {
		this.ID = ID;
		this.shelves = shelves;
	}

	public int getSize() {
		return 0;
	}

	public int getCheckoutNumber(Player p) {
		return this.checkouts.get(p).size();
	}

	public int maxCheckoutNumber() {
		return (int) Math.log(this.getSize());
	}

	public boolean canCheckOut(Player p) {
		return this.getCheckoutNumber(p) < this.maxCheckoutNumber();
	}

	public void returnBook(Book b, Player p) {
		this.checkouts.get(p).remove(b);
	}

	private class Book {
		String BML;
	}

}
