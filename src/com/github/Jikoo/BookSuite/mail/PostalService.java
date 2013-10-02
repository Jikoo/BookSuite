/*******************************************************************************
 * Copyright (c) 2013 Ted Meyer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn - minor contributions + plugin surrounding Mail
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

import org.bukkit.entity.Player;

import com.github.Jikoo.BookSuite.BSLogger;
import com.github.Jikoo.BookSuite.BookSuite;

public class PostalService {

	// the inventory of the postal service
	private Map<String, List<BookMailWrapper>> inventory;

	/*
	 * Singleton instance
	 */
	private static PostalService instance;

	public static PostalService getInstance() {
		if (instance == null) {
			instance = new PostalService();
		}
		return instance;
	}

	/**
	 * creates a new instance of the postal service if there was not one already
	 * in existence in file.
	 */
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
			this.inventory = new HashMap<String, List<BookMailWrapper>>();
			this.inventory = recovered;
			input.close();

		} catch (Exception e) {
			BSLogger.debugInfo("No mail was saved previously.");
		}
	}

	/**
	 * 
	 * @param mail
	 *            a list of mail wrappers that can be added to the inventory
	 */
	public void collect(List<BookMailWrapper> mail) {
		for (BookMailWrapper bmw : mail) {
			this.collect(bmw);
		}
	}

	/**
	 * 
	 * @param bmw
	 *            a single mail wrapper that can be added to the inventory
	 */
	public void collect(BookMailWrapper bmw) {
		if (this.inventory.get(bmw.getAdressee()) == null) {
			this.inventory.put(bmw.getAdressee(),
					new LinkedList<BookMailWrapper>());
		}

		this.inventory.get(bmw.getAdressee()).add(bmw);
	}

	/**
	 * 
	 * @param p
	 *            the player to whom the mail is being distributed
	 * @param amount
	 *            the amount of space that the mailbox has free and is to be
	 *            filled
	 * @return a list of book wrappers that is less than or equal to the
	 *         provided maximum amount, and all addressed to the player provided
	 */
	public List<BookMailWrapper> distribute(Player p, int amount) {
		return this.distribute(p.getName(), amount);
	}

	public List<BookMailWrapper> distribute(String p, int amount) {
		List<BookMailWrapper> mail = new LinkedList<BookMailWrapper>();
		List<BookMailWrapper> allMail = this.inventory.get(p);
		while (mail.size() < amount && allMail != null && allMail.size() > 0) {
			mail.add(allMail.remove(0));
		}

		return mail;
	}

	/**
	 * writes the postal service object to file so that it can be retrieved
	 * later
	 */
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
			BSLogger.severe("None of the mail data was saved!");
			BSLogger.err(ex);
		}
	}

	/**
	 * Saves any remaining data and sets inventory to null.
	 */
	private void save() {
		this.writeToFile();
		this.inventory = null;
	}

	/**
	 * disables the module of the plugin: if postal service has been accessed,
	 * save it to file, otherwise, do nothing
	 */
	public static void disable() {
		if (instance != null) {
			instance.save();
			instance = null;
		}
	}
}
