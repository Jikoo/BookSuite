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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import com.github.Jikoo.BookSuite.BookSuite;

public class PostalService {
	Map<String, List<BookMailWrapper>> inventory = new HashMap<String, List<BookMailWrapper>>();

	private static PostalService instance;

	public static PostalService getInstance() {
		if (instance == null) {
			instance = new PostalService();
		}
		return instance;
	}

	public PostalService() {
		try {
			// use buffering
			InputStream file = new FileInputStream(BookSuite.getInstance()
					.getDataFolder() + "/mail.post");
			InputStream buffer = new BufferedInputStream(file);
			ObjectInput input = new ObjectInputStream(buffer);
			@SuppressWarnings("unchecked")
			Map<String, List<BookMailWrapper>> recovered = (Map<String, List<BookMailWrapper>>) input
					.readObject();
			this.inventory = recovered;
			input.close();

		} catch (Exception e) {
			BookSuite.getInstance().getServer().getLogger()
					.log(Level.FINE, "no previous mail");
		}
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

	public void writeToFile() {
		try {
			OutputStream file = new FileOutputStream(BookSuite.getInstance()
					.getDataFolder() + "/mail.post");
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			try {
				output.writeObject(inventory);
			} finally {
				output.close();
			}
		} catch (IOException ex) {
			BookSuite.getInstance().getServer().getLogger()
					.log(Level.SEVERE, "none of the mail data was saved!!!");
			ex.printStackTrace();
		}
	}
}
