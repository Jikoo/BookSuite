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

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.jikoo.booksuite.update.UpdateConfig;
import com.github.jikoo.booksuite.update.UpdateStrings;

public class BookSuite extends JavaPlugin {

	private Messages msgs;
	private Functions functions;
	private FileManager filemanager;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		if (new UpdateConfig(this).update()) {
			BSLogger.warn("Your configuration has been updated, please check it!");
		}
		if (new UpdateStrings(this).update()) {
			BSLogger.info("More customization has been added to strings.yml.");
		}

		msgs = new Messages(this);
		functions = new Functions(this);
		filemanager = new FileManager(this);

		getServer().getPluginManager().registerEvents(new MainListener(this), this);
		getCommand("book").setExecutor(new CommandHandler(this));
	}

	public FileManager getFileManager() {
		return this.filemanager;
	}

	public Functions getFunctions() {
		return this.functions;
	}

	public Messages getMessages() {
		return this.msgs;
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
	}
}
