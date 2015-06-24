/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn - ideas and implementation
 *     Ted Meyer - IO assistance and BML (Book Markup Language)
 ******************************************************************************/
package com.github.jikoo.booksuite;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.jikoo.booksuite.permissions.PermissionsListener;
import com.github.jikoo.booksuite.update.UpdateConfig;
import com.github.jikoo.booksuite.update.UpdateStrings;

public class BookSuite extends JavaPlugin {
	protected String version;

	public Msgs msgs;

	protected PermissionsListener perms;

	public Functions functions;
	public FileManager filemanager;

	private MainListener listener;
	private CommandHandler command;


	private static BookSuite instance;

	@Override
	public void onEnable() {
		version = this.getDescription().getVersion();

		instance = this;

		saveDefaultConfig();

		if (new UpdateConfig(this).update()) {
			BSLogger.warn("Your configuration has been updated, please check it!");
		}
		if (new UpdateStrings(this).update()) {
			BSLogger.info("More customization has been added to strings.yml.");
		}
		msgs = new Msgs();

		functions = Functions.getInstance();
		filemanager = FileManager.getInstance();

		if (getConfig().getBoolean("use-inbuilt-permissions")) {
			perms = new PermissionsListener(this);
			perms.enable();
		}

		listener = MainListener.getInstance();
		getServer().getPluginManager().registerEvents(listener, this);
		command = CommandHandler.getInstance();
		getCommand("book").setExecutor(command);

		BSLogger.info(new StringBuilder("BookSuite v").append(version).append(" enabled").toString());
	}

	@Override
	public void onDisable() {

		if (perms != null)
			perms.disable();
		perms = null;

		command = null;

		functions.disable();
		functions = null;

		filemanager.disable();
		filemanager = null;

		listener.disable();
		listener = null;

		instance = null;

		BSLogger.info(new StringBuilder("BookSuite v").append(version).append(" disabled").toString());
	}

	public static BookSuite getInstance() {
		return instance;
	}
}
