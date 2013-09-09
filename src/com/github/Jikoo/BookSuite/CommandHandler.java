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

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.github.Jikoo.BookSuite.metrics.Metrics;
import com.github.Jikoo.BookSuite.permissions.PermissionsListener;
import com.github.Jikoo.BookSuite.rules.Rules;
import com.github.Jikoo.BookSuite.update.UpdateCheck;
import com.github.Jikoo.BookSuite.update.UpdateConfig;
import com.github.Jikoo.BookSuite.update.UpdateStrings;

public class CommandHandler implements CommandExecutor {

	BookSuite plugin = BookSuite.getInstance();
	HashMap<String, String> overwritable = new HashMap<String, String>();

	private static CommandHandler instance;

	public static CommandHandler getInstance() {
		if (instance == null)
			instance = new CommandHandler();
		return instance;
	}

	public enum CommandPermissions {
		EDIT, AUTHOR, TITLE, COPY, UNSIGN, IMPORT, EXPORT, LIST, DELETE, RELOAD, UPDATE;

		public boolean checkPermission(CommandSender s) {
			return (s.hasPermission("booksuite.command." + this.lName()) || !(s instanceof Player));
		}

		public String lName() {
			return this.name().toLowerCase();
		}

		public static CommandPermissions uValueOf(String s) {
			return (CommandPermissions.valueOf(s.toUpperCase()));
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			String[] args) {
		if (args.length >= 1) {
			args[0] = args[0].toLowerCase();

			if (args[0].equals("reload")
					&& CommandPermissions.RELOAD.checkPermission(sender)) {
				reload(sender);
				return true;
			}
			if (args[0].equals("update")
					&& CommandPermissions.UPDATE.checkPermission(sender)) {
				plugin.update.delayUpdateCheck(sender, true, 0L);
				return true;
			}
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.msgs.get("VERSION")
					.replace("<plugin.version>", plugin.version));
			sender.sendMessage(plugin.msgs.get("HELP_CONSOLE_RELOAD"));
			sender.sendMessage(plugin.msgs.get("HELP_CONSOLE_UPDATE"));
			return true;
		}

		// PLAYER-ONLY COMMANDS
		Player p = (Player) sender;

		if (args.length >= 1) {
			// command: /book <usage|help> - prints out usage and help based
			// on additional args
			if (args[0].equals("usage") || args[0].equals("help")) {
				if (usage(p, args))
					return true;
			}

			// command: /book copy (quantity) - attempts to make specified
			// quantity of copies, default 1.
			if (args[0].equals("copy")) {
				if (copyItem(p, args))
					return true;
			}

			/*
			 * command: /book overwrite (book) - if a save was attempted,
			 * will save over file with book in hand. If no save was
			 * attempted, will save and overwrite any book by the specified
			 * name. Usage instead of /book save is discouraged for obvious
			 * reasons.
			 */
			if (args[0].equals("overwrite")) {
				if (overwrite(p, args))
					return true;
			}
		} else {
			return this.invalidCommand(sender);
		}

		if (args.length == 1) {
			// command: /book <l(ist)|ls> - list all files in
			// /SavedBooks/
			if (args[0].equals("l") || args[0].equals("list")
					|| args[0].equals("ls")) {
				if (CommandPermissions.LIST.checkPermission(p)) {
					if (args.length == 1) {
						plugin.functions.listBookFilesIn(plugin.getDataFolder()
								+ "/SavedBooks/", p);
						return true;
					}
				}
			}

			// command: /book u(nsign) - attempt to unsign book
			if (args[0].equals("u") || args[0].equals("unsign")) {
				if (unsign(p))
					return true;
			}
			return this.invalidCommand(sender);
		}
		if (args.length == 2) {
			// command: /book <e(xport)|s(ave)> <filename> - attempt
			// to save book in hand to file
			if (args[0].equals("e") || args[0].equals("export")
					|| args[0].equals("s")
					|| args[0].equals("save")) {
				if (export(p, args))
					return true;
			}

			// command: /book <u(rl)|<f(ile)|l(oad)>> <url|filename>
			// - attempt to import a book from location
			if ((args[0].equals("f") || args[0].equals("file")
					|| args[0].equals("l") || args[0]
						.equals("load"))
					&& CommandPermissions.IMPORT.checkPermission(p)) {
				ItemStack newbook = new ItemStack(
						Material.WRITTEN_BOOK, 1);
				newbook.setItemMeta(plugin.filemanager.makeBookMetaFromText(
						p, args[1], plugin.getDataFolder()
								+ "/SavedBooks/", true));
				if (!newbook.hasItemMeta()
						|| newbook.getItemMeta() == null) {
					p.sendMessage(plugin.msgs.get("FAILURE_FILE_NONEXISTANT"));
				} else {
					if (plugin.functions.canObtainBook(p)) {
						p.getInventory().addItem(newbook);
						p.sendMessage(plugin.msgs.get("SUCCESS_IMPORT")
								.replace("<book.savename>", args[1]));
					}
				}
				return true;
			} else if ((args[0].equals("u") || args[0]
					.equals("url"))
					&& CommandPermissions.IMPORT.checkPermission(p)) {
				if (!plugin.functions.canObtainBook(p))
					return true;
				else {
					asyncBookImport(p.getName(), args[1], plugin
							.getDataFolder().getPath());
					p.sendMessage(plugin.msgs.get("SUCCESS_IMPORT_INITIATED"));
				}
				return true;
			}

			// command: /book d(elete) <filename> - attempt to
			// delete file
			if (args[0].equals("d") || args[0].equals("delete")) {
				if (CommandPermissions.DELETE.checkPermission(p)) {
					if (args[1].contains("."))
						plugin.filemanager.delete(plugin.getDataFolder()
								+ "/SavedBooks/", args[1]);
					else
						plugin.filemanager.delete(plugin.getDataFolder()
								+ "/SavedBooks/", args[1] + ".book");
					p.sendMessage(plugin.msgs.get("SUCCESS_DELETE").replace("<file.name>", args[1]));
					return true;
				}
			}
			return this.invalidCommand(sender);
		}

		if (args.length >= 2) {

			// command: /book t(itle) <args> - attempt to change title
			// with additional args. Include spaces.
			if (args[0].equals("t") || args[0].equals("title")) {
				if (title(p, args))
					return true;
			}

			// command: /book addpage <number> (optional text) - add a
			// page to a book and quill
			if (args[0].equals("addpage")) {
				if (CommandPermissions.EDIT.checkPermission(p)) {
					String text = "";
					for (int i = 2; i < args.length; i++) {
						if (i != (args.length - 1))
							text += args[i] + " ";
						else
							text += args[i];
					}
					if (plugin.functions.insertPageAt(p, args[1], text))
						p.sendMessage(plugin.msgs.get("SUCCESS_EDIT_ADDPAGE"));
					return true;
				}
			}

			// command: /book delpage <number> - remove a page from a
			// book and quill
			if (args[0].equals("delpage")) {
				if (CommandPermissions.EDIT.checkPermission(p)) {
					if (plugin.functions.deletePageAt(p, args[1]))
						p.sendMessage(plugin.msgs.get("SUCCESS_EDIT_DELPAGE"));
					return true;
				}
			}

			// command: /book a(uthor) <args> - attempt to change author
			// with additional args. Include spaces.
			if (args[0].equals("a") || args[0].equals("author")) {
				if (CommandPermissions.AUTHOR.checkPermission(p)) {
					String newAuthor = "";
					for (int i = 1; i < args.length; i++)
						if (i != (args.length - 1))
							newAuthor += args[i] + " ";
						else
							newAuthor += args[i];
					if (plugin.functions.setAuthor(p, newAuthor))
						p.sendMessage(plugin.msgs.get("SUCCESS_AUTHOR"));
					else
						p.sendMessage(plugin.msgs.get("FAILURE_COMMAND_NEEDBOOK"));
					return true;
				}
			}
		}

		return this.invalidCommand(sender);
	}

	public boolean title(Player p, String[] args) {
		if (CommandPermissions.TITLE.checkPermission(p)) {
			if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)) {
				p.sendMessage(plugin.msgs.get("FAILURE_COMMAND_NEEDBOOK"));
			}
			if (plugin.functions.isAuthor(p, ((BookMeta)p.getItemInHand().getItemMeta()).getAuthor())
					|| p.hasPermission("booksuite.command.title.other")) {
				String newTitle = "";
				for (int i = 1; i < args.length; i++) {
					newTitle += args[i];
					if (i == (args.length - 1))
						newTitle += " ";
				}
				plugin.functions.setTitle(p, newTitle);
				p.sendMessage(plugin.msgs.get("SUCCESS_TITLE"));
			} else {
				p.sendMessage(plugin.msgs.get("FAILURE_PERMISSION_TITLE_OTHER"));
			}
			return true;
		}
		return false;
	}

	public boolean unsign(Player p) {
		if (CommandPermissions.UNSIGN.checkPermission(p)) {
			if (!p.getItemInHand().getType().equals(Material.BOOK_AND_QUILL)
					&& !p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)) {
				p.sendMessage(plugin.msgs.get("FAILURE_COMMAND_NEEDEITHER"));
			}
			if (plugin.functions.isAuthor(p, ((BookMeta)p.getItemInHand().getItemMeta()).getAuthor())
					|| p.hasPermission("booksuite.command.unsign.other")) {
				plugin.functions.unsign(p);
				p.sendMessage(plugin.msgs.get("SUCCESS_UNSIGN"));
			} else
				p.sendMessage(plugin.msgs.get("FAILURE_PERMISSION_UNSIGN_OTHER"));
			return true;
		} else
			return false;
	}

	public void reload(CommandSender sender) {
		plugin.reloadConfig();

		if (new UpdateConfig(plugin).update()) {
			BSLogger.warn("Your configuration has been updated, please check it!");
		}
		if (new UpdateStrings(plugin).update()) {
			BSLogger.info("More customization has been added to strings.yml.");
		}
		plugin.msgs = new Msgs();
		plugin.alias.enable();

		if (plugin.getConfig().getBoolean("use-inbuilt-permissions")) {
			if (plugin.perms != null) {
				if (!plugin.perms.isEnabled()) {
					plugin.perms.enable();
				} else {
					plugin.perms.disable();
					plugin.perms.enable();
				}
			} else {
				plugin.perms = new PermissionsListener(plugin);
				plugin.perms.enable();
			}
		} else {
			if (plugin.perms != null) {
				plugin.perms.disable();
				plugin.perms = null;
			}
		}

		try {
			if (plugin.getConfig().getBoolean("enable-metrics")) {
				if (plugin.metrics == null) {
					plugin.metrics = new Metrics(plugin);
					plugin.metrics.start();
				} else {
					plugin.metrics.start();
				}
			} else {
				if (plugin.metrics != null) {
					plugin.metrics.disable();
					plugin.metrics = null;
				}
			}
		} catch (Exception e) {
			BSLogger.warn("[BookSuite] Error changing metrics settings.");
			BSLogger.err(e);
		}

		if (plugin.getConfig().getBoolean("login-update-check")) {
			if (plugin.update == null)
				plugin.update = new UpdateCheck();
			plugin.update.disableNotifications();
			plugin.update.enableNotifications();
		} else {
			if (plugin.update != null) {
				plugin.update.disableNotifications();
				plugin.update = null;
			}
		}

		plugin.alias.enable();

		if (plugin.getConfig().getBoolean("book-rules")) {
			if (plugin.rules == null) {
				plugin.rules = new Rules();
				plugin.rules.enable();
			} else
				plugin.rules.load();
		} else if (plugin.rules != null) {
			plugin.rules.disable();
			plugin.rules = null;
		}

		if (new File(plugin.getDataFolder(), "temp").exists())
			plugin.filemanager.delete(plugin.getDataFolder().getPath(), "temp");
		sender.sendMessage(ChatColor.AQUA + "BookSuite v"
				+ ChatColor.DARK_PURPLE + plugin.version + ChatColor.AQUA
				+ " reloaded!");
	}

	public boolean copyItem(Player p, String[] args) {
		if (CommandPermissions.COPY.checkPermission(p)) {
			int copies;
			if (args.length >= 2) {
				try {
					copies = Integer.parseInt(args[1]);
					if (copies > plugin.getConfig().getInt(
							"maximum-copies-per-operation")) {
						copies = plugin.getConfig().getInt(
								"maximum-copies-per-operation");
						p.sendMessage(ChatColor.DARK_RED
								+ "The maximum number of books copiable at once is "
								+ copies + ".");
					}
				} catch (NumberFormatException e) {
					p.sendMessage(ChatColor.DARK_RED + args[1]
							+ " is not a valid integer. Assuming 1..");
					copies = 1;
				}
			} else
				copies = 1;
			boolean completed = true;
			ItemStack is = p.getItemInHand();
			if (is.getType().equals(Material.MAP)) {
				for (int i = 0; i < copies; i++) {
					if (plugin.functions.canObtainMap(p))
						plugin.functions.copy(p);
					else {
						completed = false;
						break;
					}
				}
				if (completed)
					p.sendMessage(plugin.msgs.get("SUCCESS_COPY"));
				return true;
			} else if (!is.hasItemMeta() || is.getItemMeta() == null) {
				p.sendMessage(plugin.msgs.get("FAILURE_COPY_UNCOPIABLE"));
			} else if (is.getType().equals(Material.WRITTEN_BOOK)) {
				BookMeta bm = (BookMeta) is.getItemMeta();
				if (plugin.functions.checkCommandCopyPermission(p, bm.getAuthor())) {
					for (int i = 0; i < copies; i++) {
						if (plugin.functions.canObtainBook(p))
							plugin.functions.copy(p);
						else {
							completed = false;
							break;
						}
					}
					if (completed)
						p.sendMessage(plugin.msgs.get("SUCCESS_COPY"));
				}
				return true;
			} else if (is.getType().equals(Material.BOOK_AND_QUILL)) {
				if (p.hasPermission("booksuite.copy.unsigned")) {
					for (int i = 0; i < copies; i++) {
						if (plugin.functions.canObtainBook(p))
							plugin.functions.copy(p);
						else {
							completed = false;
							break;
						}
					}
					if (completed)
						p.sendMessage(plugin.msgs.get("SUCCESS_COPY"));
				} else
					p.sendMessage(plugin.msgs.get("FAILURE_PERMISSION_COPY"));
				return true;
			} else
				p.sendMessage(plugin.msgs.get("FAILURE_COPY_UNCOPIABLE"));
			return true;
		} else
			return false;
	}

	public boolean export(Player p, String[] args) {
		if (CommandPermissions.EXPORT.checkPermission(p)) {
			if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)) {
				p.sendMessage(plugin.msgs.get("FAILURE_COMMAND_NEEDBOOK"));
				return true;
			}
			BookMeta bm = (BookMeta) p.getItemInHand().getItemMeta();
			if (plugin.filemanager.makeFileFromBookMeta(bm, plugin.getDataFolder()
					+ "/SavedBooks/", args[1])) {
				p.sendMessage(plugin.msgs.get("SUCCESS_EXPORT")
						.replace("<book.savename>", args[1]));
			} else {
				p.sendMessage(plugin.msgs.get("FAILURE_FILE_EXISTANT"));
				if (p.hasPermission("booksuite.command.delete")) {
					p.sendMessage(plugin.msgs.get("OVERWRITE_FILE")
							.replace("<book.savename>", args[1]));
					overwritable.put(p.getName(), args[1]);
					syncOverwriteTimer(p);
				}
			}
			return true;
		} else
			return false;
	}

	public boolean overwrite(Player p, String[] args) {
		if (CommandPermissions.DELETE.checkPermission(p)
				&& CommandPermissions.EXPORT.checkPermission(p)) {
			if (!p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)) {
				p.sendMessage(plugin.msgs.get("FAILURE_COMMAND_NEEDBOOK"));
				return true;
			} else {
				if (overwritable.containsKey(p.getName())) {
					if (plugin.filemanager.delete(
							plugin.getDataFolder() + "/SavedBooks/",
							overwritable.get(p.getName()))) {
						if (plugin.filemanager.makeFileFromBookMeta((BookMeta) p
								.getItemInHand().getItemMeta(),
								plugin.getDataFolder() + "/SavedBooks/",
								overwritable.get(p.getName()))) {
							p.sendMessage(plugin.msgs.get("SUCCESS_EXPORT")
									.replace("<book.savename>", overwritable.get(p.getName())));
						}
					}
					overwritable.remove(p.getName());
					return true;
				} else {
					if (args.length == 2) {
						if (!plugin.filemanager.delete(plugin.getDataFolder()
								+ "/SavedBooks/", args[1])) {
							p.sendMessage(plugin.msgs.get("OVERWRITE_WARN"));
						}
						if (plugin.filemanager.makeFileFromBookMeta((BookMeta) p
								.getItemInHand().getItemMeta(),
								plugin.getDataFolder() + "/SavedBooks/",
								args[1])) {
							p.sendMessage(plugin.msgs.get("SUCCESS_EXPORT"));
						}
					} else
						p.sendMessage(plugin.msgs.get("FAILURE_OVERWRITE"));
					return true;
				}
			}
		} else
			return false;
	}

	public boolean usage(Player p, String[] args) {
		if (listPermittedCommands(p).length() > 0) {
			if (args.length == 1) {
				p.sendMessage(plugin.msgs.get("USAGE"));
				p.sendMessage(ChatColor.DARK_RED + listPermittedCommands(p));
			} else {
				boolean failure = false;
				StringBuilder sb1 = new StringBuilder();
				for (String s : args) {
					s = s.replaceAll("\\W\\z", "");
					if (s.equals(args[0])) {} else {
						try {
							CommandPermissions cdp = CommandPermissions
									.uValueOf(s);
							if (cdp.checkPermission(p)) {
								switch (cdp) {
								case EDIT:
									p.sendMessage(plugin.msgs.get("USAGE_EDIT_ADDPAGE")
											+ plugin.msgs.get("USAGE_EDIT_ADDPAGE_EXPLANATION"));
									p.sendMessage(plugin.msgs.get("USAGE_EXAMPLE")
											+ plugin.msgs.get("USAGE_EDIT_ADDPAGE_EXAMPLE"));
									p.sendMessage(plugin.msgs.get("USAGE_EDIT_DELPAGE")
											+ plugin.msgs.get("USAGE_EDIT_DELPAGE_EXPLANATION"));
									p.sendMessage(plugin.msgs.get("USAGE_EXAMPLE")
											+ plugin.msgs.get("USAGE_EDIT_DELPAGE_EXAMPLE"));
									break;
								case AUTHOR:
									p.sendMessage(plugin.msgs.get("USAGE_AUTHOR")
											+ plugin.msgs.get("USAGE_AUTHOR_EXPLANATION"));
									p.sendMessage(plugin.msgs.get("USAGE_EXAMPLE")
											+ plugin.msgs.get("USAGE_AUTHOR_EXAMPLE"));
									break;
								case TITLE:
									p.sendMessage(ChatColor.AQUA
											+ "/book t(itle) <new title>"
											+ ChatColor.DARK_GREEN
											+ " - change title of book in hand");
									p.sendMessage(ChatColor.DARK_GREEN
											+ "Usage example: "
											+ ChatColor.AQUA
											+ "/book t BookSuite Instruction Manual");
									break;
								case COPY:
									p.sendMessage(ChatColor.AQUA
											+ "/book copy <quantity>"
											+ ChatColor.DARK_GREEN
											+ " - Create copies!");
									p.sendMessage(ChatColor.DARK_GREEN
											+ "Usage example: "
											+ ChatColor.AQUA + "/book copy 20");
									break;
								case UNSIGN:
									p.sendMessage(ChatColor.AQUA
											+ "/book u(nsign)"
											+ ChatColor.DARK_GREEN
											+ " - unsign book in hand.");
									p.sendMessage(ChatColor.DARK_GREEN
											+ "Usage example: "
											+ ChatColor.AQUA + "/book u");
									break;
								case IMPORT:
									p.sendMessage(ChatColor.AQUA
											+ "/book <u(rl)|<f(ile)|l(oad)>> <url|filename>"
											+ ChatColor.DARK_GREEN
											+ " - import book from file or url");
									p.sendMessage(ChatColor.DARK_GREEN
											+ "Usage example: "
											+ ChatColor.AQUA
											+ "/book u http://dev.bukkit.org/paste/gy7ekjupawivnbxq.txt");
									break;
								case EXPORT:
									p.sendMessage(ChatColor.AQUA
											+ "/book <e(xport)|s(ave)> <filename>"
											+ ChatColor.DARK_GREEN
											+ " - export held book to file");
									p.sendMessage(ChatColor.DARK_GREEN
											+ "Usage example: "
											+ ChatColor.AQUA
											+ "/book s ExampletasticBook");
									break;
								case LIST:
									p.sendMessage(ChatColor.AQUA
											+ "/book l(ist)"
											+ ChatColor.DARK_GREEN
											+ " - list all books");
									p.sendMessage(ChatColor.DARK_GREEN
											+ "Usage example: "
											+ ChatColor.AQUA + "/book l");
									break;
								case DELETE:
									p.sendMessage(ChatColor.AQUA
											+ "/book d(elete) <file>"
											+ ChatColor.DARK_GREEN
											+ " - delete specified book");
									p.sendMessage(ChatColor.DARK_GREEN
											+ "Usage example: "
											+ ChatColor.AQUA
											+ "/book d ExampletasticBook");
									break;
								case RELOAD:
									p.sendMessage(ChatColor.AQUA
											+ "/book reload"
											+ ChatColor.DARK_GREEN
											+ " - reload the plugin");
									p.sendMessage(ChatColor.DARK_GREEN
											+ "Usage example: "
											+ ChatColor.AQUA + "/book reload");
									break;
								case UPDATE:
									p.sendMessage(ChatColor.AQUA
											+ "/book update"
											+ ChatColor.DARK_GREEN
											+ " - check for updates");
									p.sendMessage(ChatColor.DARK_GREEN
											+ "Usage example: "
											+ ChatColor.AQUA + "/book update");
									break;
								default:
									failure = true;
									sb1.append(s + ", ");
									break;
								}
							} else {
								failure = true;
								sb1.append(s + ", ");
							}
						} catch (IllegalArgumentException e) {
							if ((s.equalsIgnoreCase("press") || s
									.equalsIgnoreCase("printingpress"))
									&& (p.hasPermission("booksuite.copy.self")
											|| p.hasPermission("booksuite.copy.unsigned") || p
												.hasPermission("booksuite.copy.map"))) {
								p.sendMessage(ChatColor.DARK_GREEN + "A "
										+ ChatColor.AQUA + "printing press"
										+ ChatColor.DARK_GREEN
										+ " is made by placing inverted "
										+ ChatColor.AQUA + "stairs"
										+ ChatColor.DARK_GREEN + " over a "
										+ ChatColor.AQUA + "crafting table"
										+ ChatColor.DARK_GREEN + ".");
								if (p.hasPermission("booksuite.copy.createpress"))
									p.sendMessage(ChatColor.DARK_GREEN
											+ "Right click the top of a "
											+ ChatColor.AQUA + "crafting table"
											+ ChatColor.DARK_GREEN
											+ " holding " + ChatColor.AQUA
											+ "stairs" + ChatColor.DARK_GREEN
											+ " to easily assemble one!");
								p.sendMessage(ChatColor.DARK_GREEN
										+ "To use a press, right click it with a copiable item.");
								StringBuilder sb2 = new StringBuilder();
								sb2.append(ChatColor.DARK_GREEN
										+ "Copiable items: ");
								if (p.hasPermission("booksuite.copy.self"))
									sb2.append(ChatColor.AQUA + "Written Book"
											+ ChatColor.DARK_GREEN + ", ");
								if (p.hasPermission("booksuite.copy.unsigned"))
									sb2.append(ChatColor.AQUA
											+ "Book and Quill"
											+ ChatColor.DARK_GREEN + ", ");
								if (p.hasPermission("booksuite.copy.map"))
									sb2.append(ChatColor.AQUA + "Map<3");
								p.sendMessage(sb2.substring(0, sb2.length() - 2));
							} else if ((s.equalsIgnoreCase("erase") || s
									.equalsIgnoreCase("eraser"))
									&& p.hasPermission("booksuite.block.erase")) {
								p.sendMessage(ChatColor.DARK_GREEN + "An "
										+ ChatColor.AQUA + "eraser"
										+ ChatColor.DARK_GREEN + " is a "
										+ ChatColor.AQUA + "cauldron"
										+ ChatColor.DARK_GREEN
										+ ". Right click one with a "
										+ ChatColor.AQUA + "Written Book"
										+ ChatColor.DARK_GREEN + " to unsign!");
								if (!p.hasPermission("booksuite.block.erase.free"))
									p.sendMessage(ChatColor.DARK_GREEN
											+ "Erasing books consumes "
											+ ChatColor.AQUA + "water"
											+ ChatColor.DARK_GREEN + ".");
							} else {
								failure = true;
								sb1.append(s + ", ");
							}
						}
					}
				}

				if (failure) {
					if (sb1.substring(0, sb1.length() - 2).contains(", "))
						p.sendMessage(ChatColor.DARK_RED
								+ "Invalid help topics: "
								+ sb1.substring(0, sb1.length() - 2));
					else
						p.sendMessage(ChatColor.DARK_RED
								+ "Invalid help topic: "
								+ sb1.substring(0, sb1.length() - 2));
					p.sendMessage(ChatColor.DARK_RED
							+ "Possible topics are as follows:");
					p.sendMessage(ChatColor.DARK_RED + listPermittedCommands(p));
				}
			}
			return true;
		}
		return false;
	}

	public String listPermittedCommands(CommandSender s) {
		StringBuilder sb = new StringBuilder();

		if (s.hasPermission("booksuite.copy.self"))
			sb.append(ChatColor.AQUA + "printingpress" + ChatColor.DARK_GREEN
					+ ", ");
		if (s.hasPermission("booksuite.block.erase"))
			sb.append(ChatColor.AQUA + "eraser" + ChatColor.DARK_GREEN + ", ");
		for (CommandPermissions i : CommandPermissions.values()) {
			if (i.checkPermission(s))
				sb.append(ChatColor.AQUA + i.lName() + ChatColor.DARK_GREEN
						+ ", ");
		}

		return sb.substring(0, sb.length() - 2);
	}

	public boolean invalidCommand(CommandSender sender) {
		sender.sendMessage(plugin.msgs.get("VERSION")
				.replaceAll("<plugin.version>", plugin.version));
		if (listPermittedCommands(sender).length() > 0) {
			sender.sendMessage(ChatColor.DARK_GREEN + "For command usage, use "
					+ ChatColor.AQUA + "/book help <topic(s)>"
					+ ChatColor.DARK_GREEN + ".");
			sender.sendMessage(ChatColor.DARK_GREEN + "Possible topics: "
					+ listPermittedCommands(sender));
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public void asyncBookImport(String p, String s, String dir) {
		Bukkit.getServer().getScheduler()
				.scheduleAsyncDelayedTask(plugin, new getStreamBook(p, s, dir));
	}

	public class getStreamBook implements Runnable {
		String p;
		URL url;
		String loc;

		getStreamBook(String p, String s, String dir) {
			this.p = p;
			loc = dir;
			try {
				url = new URL(s);
			} catch (MalformedURLException e) {}
		}

		public void run() {
			File dir = new File(loc + "/temp/");
			if (!dir.exists())
				dir.mkdirs();
			File tempFile;
			for (int i = 1; i <= 5; i++) {
				tempFile = new File(dir, "temp" + i + ".book");
				if (!tempFile.exists()) {
					try {
						tempFile.createNewFile();
						Scanner urlInput = new Scanner(url.openStream());
						FileWriter tempWriter = new FileWriter(tempFile);
						while (urlInput.hasNextLine()) {
							tempWriter.append(urlInput.nextLine() + "\n");
						}
						urlInput.close();
						tempWriter.close();
					} catch (Exception e) {
						if (tempFile.exists())
							tempFile.delete();
						return;
					}
					syncBookImport(p, i);
					return;
				} else if (i == 5)
					syncBookImport(p, -1);
			}
		}
	}

	public void syncBookImport(String p, int temp) {
		Bukkit.getServer().getScheduler()
				.scheduleSyncDelayedTask(plugin, new giveStreamBook(p, temp));
	}

	public class giveStreamBook implements Runnable {
		Player p;
		int temp;
		FileManager fm = FileManager.getInstance();

		giveStreamBook(String p, int temp) {
			this.p = plugin.getServer().getPlayer(p);
			this.temp = temp;
		}

		public void run() {
			if (temp == -1) {
				p.sendMessage(ChatColor.DARK_RED
						+ "Too many books are being imported at this time, please try again later.");
				return;
			}
			BookMeta bm = fm.makeBookMetaFromText(p, "temp" + temp,
					plugin.getDataFolder() + "/temp/", true);
			fm.delete(plugin.getDataFolder() + "/temp/", "temp" + temp
					+ ".book");
			if (bm != null) {
				ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
				is.setItemMeta(bm);
				if (p.getInventory().firstEmpty() != -1) {
					p.getInventory().addItem(is);
				} else {
					p.getWorld().dropItem(p.getLocation(), is);
				}
			} else {
				p.sendMessage(ChatColor.DARK_RED + "Error reading from URL.");
				if (p.getInventory().firstEmpty() > 0) {
					p.getInventory().addItem(
							new ItemStack(Material.INK_SACK, 1));
					p.getInventory().addItem(new ItemStack(Material.BOOK, 1));
				} else {
					p.sendMessage(ChatColor.DARK_RED
							+ "Dropped book supplies at your feet.");
					p.getWorld().dropItem(p.getLocation(),
							new ItemStack(Material.INK_SACK, 1));
					p.getWorld().dropItem(p.getLocation(),
							new ItemStack(Material.BOOK, 1));
				}
			}
		}
	}

	public void syncOverwriteTimer(Player p) {
		Bukkit.getServer().getScheduler()
				.scheduleSyncDelayedTask(plugin, new overwriteTimer(p), 200L);
	}

	public class overwriteTimer implements Runnable {
		Player p;

		overwriteTimer(Player p) {
			this.p = p;
		}

		public void run() {
			if (overwritable.containsKey(p.getName())) {
				overwritable.remove(p.getName());
				p.sendMessage(ChatColor.DARK_RED + "Overwrite time expired!");
			}
		}
	}
}
