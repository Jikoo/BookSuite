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

import org.bukkit.entity.Player;

public class PostalService {
	Map<String, List<BookMailWrapper>> inventory = new HashMap<String, List<BookMailWrapper>>();

	private static PostalService instance;

	public static PostalService getInstance() {
		if (instance == null) {
			instance = new PostalService();
		}
		return instance;
	}

	public void collect(List<BookMailWrapper> mail) {
		for (BookMailWrapper bmw : mail) {
			this.collect(bmw);
		}
	}

	public void collect(BookMailWrapper bmw) {
		if (this.inventory.get(bmw.getAdressee()) == null) {
			this.inventory.put(bmw.getAdressee(),
					new LinkedList<BookMailWrapper>());
		}
		this.inventory.get(bmw.getAdressee()).add(bmw);
	}

	public List<BookMailWrapper> distribute(Player p, int amount) {
		return this.distribute(p.getName(), amount);
	}

	public List<BookMailWrapper> distribute(String p, int amount) {
		List<BookMailWrapper> mail = new LinkedList<BookMailWrapper>();
		List<BookMailWrapper> allMail = this.inventory.get(p);
		while (mail.size() <= amount && allMail != null && allMail.size() > 0) {
			mail.add(allMail.remove(0));
		}

		return mail;
	}
}
