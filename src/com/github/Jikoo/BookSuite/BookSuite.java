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
package com.github.Jikoo.BookSuite;

import org.bukkit.plugin.java.JavaPlugin;

import com.github.Jikoo.BookSuite.mail.PostalService;
import com.github.Jikoo.BookSuite.metrics.Metrics;
import com.github.Jikoo.BookSuite.permissions.PermissionsListener;
import com.github.Jikoo.BookSuite.rules.Rules;
import com.github.Jikoo.BookSuite.update.UpdateCheck;
import com.github.Jikoo.BookSuite.update.UpdateConfig;
import com.github.Jikoo.BookSuite.update.UpdateStrings;

public class BookSuite extends JavaPlugin {
	protected String version;

	public final int currentFile = 14;
	public boolean hasUpdate;
	public String updateString;

	protected UpdateCheck update;
	protected PermissionsListener perms;
	protected Rules rules;
	protected Metrics metrics;
	protected Msgs msgs;

	public Functions functions;
	public FileManager filemanager;
	public Alias alias;

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

		alias = Alias.getInstance();
		alias.enable();

		if (getConfig().getBoolean("update-check")
				|| getConfig().getBoolean("allow-update-command"))
			update = new UpdateCheck();

		try {
			if (getConfig().getBoolean("use-inbuilt-permissions")) {
				BSLogger.fine("Enabling inbuilt permissions.");
				perms = new PermissionsListener(this);
				perms.enable();
			}

			if (getConfig().getBoolean("enable-metrics")) {
				BSLogger.fine("Enabling metrics.");
				try {
					metrics = new Metrics(this);
					metrics.start();
				} catch (Exception e) {
					BSLogger.warn("Error enabling metrics.");
					BSLogger.err(e);
					if (metrics != null) {
						metrics.disable();
						metrics = null;
					}
				}
			}

			if (getConfig().getBoolean("update-check")) {
				if (getConfig().getBoolean("login-update-check")) {
					BSLogger.fine("Enabling login update check.");
					update.enableNotifications();
				}

				BSLogger.fine("Initiating update check.");

				update.asyncUpdateCheck(null, false);
			}

			if (getConfig().getBoolean("book-rules")) {
				rules = new Rules();
				rules.enable();
			}

		} catch (Exception e) {
			BSLogger.warn("Error loading configuration.");
			BSLogger.err(e);
		}

		listener = MainListener.getInstance();
		getServer().getPluginManager().registerEvents(listener, this);
		command = CommandHandler.getInstance();
		getCommand("book").setExecutor(command);

		BSLogger.info(new StringBuilder("BookSuite v").append(version)
				.append(" enabled").toString());
	}

	@Override
	public void onDisable() {
		PostalService.disable();

		try {
			if (metrics != null) {
				metrics.disable();
				metrics = null;
			}
		} catch (Exception e) {
			BSLogger.warn("Error disabling metrics.");
			BSLogger.err(e);
		}

		if (update != null) {
			update.disableNotifications();
			update = null;
		}

		if (perms != null)
			perms.disable();
		perms = null;

		alias.disable();
		alias = null;

		command = null;

		if (rules != null) {
			rules.disable();
			rules = null;
		}

		functions.disable();
		functions = null;

		filemanager.disable();
		filemanager = null;

		listener.disable();
		listener = null;

		instance = null;

		BSLogger.info(
				new StringBuilder("BookSuite v").append(version)
						.append(" disabled").toString());
	}

	public static BookSuite getInstance() {
		return instance;
	}
}
