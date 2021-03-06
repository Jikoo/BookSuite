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

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class CommandHandler implements CommandExecutor {

	private final BookSuite plugin;
	private final HashMap<String, String> overwritable = new HashMap<>();

	CommandHandler(BookSuite plugin) {
		this.plugin = plugin;
	}

	public enum CommandPermissions {
		EDIT, AUTHOR, TITLE, COPY, UNSIGN, IMPORT, GIVE, EXPORT, LIST, DELETE, RELOAD;

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

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length >= 1) {
			args[0] = args[0].toLowerCase();

			if (args[0].equals("reload") && CommandPermissions.RELOAD.checkPermission(sender)) {
				reload(sender);
				return true;
			}
			if (args.length >= 3 && args[0].matches("g(ive)?") && CommandPermissions.GIVE.checkPermission(sender)) {
				give(sender, args);
				return true;
			}
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage(plugin.getMessages().get("VERSION")
					.replace("<plugin.version>", plugin.getDescription().getVersion()));
			sender.sendMessage(plugin.getMessages().get("HELP_CONSOLE_GIVE"));
			sender.sendMessage(plugin.getMessages().get("HELP_CONSOLE_RELOAD"));
			return true;
		}

		// PLAYER-ONLY COMMANDS
		Player p = (Player) sender;

		if (args.length == 0) {
			return invalidCommand(sender);
		}

		// command: /book <usage|help>
		// prints out usage and help based on additional args
		if (args[0].matches("usage|help")) {
			usage(p, args);
			return true;
		}

		// command: /book copy (quantity)
		// attempts to make specified quantity of copies, default 1.
		if (args[0].equals("copy") && CommandPermissions.COPY.checkPermission(p)) {
			copyItem(p, args);
			return true;
		}

		/*
		 * command: /book overwrite (savename) - if a save was attempted,
		 * will save over file with book in hand. If no save was
		 * attempted, will save and overwrite any book by the specified
		 * name. Usage instead of /book save is discouraged for obvious
		 * reasons.
		 */
		if (args[0].equals("overwrite") && CommandPermissions.DELETE.checkPermission(p)
				&& CommandPermissions.EXPORT.checkPermission(p)) {
			overwrite(p, args);
			return true;
		}

		if (args.length == 1) {
			// command: /book <l(ist|s)> - list all files in
			// /SavedBooks/
			if (args[0].matches("l(ist|s)") && CommandPermissions.LIST.checkPermission(p)) {
				plugin.getFunctions().listBookFilesIn(plugin.getDataFolder() + "/SavedBooks/", p);
				// TODO list private other
				return true;
			}

			// command: /book u(nsign) - attempt to unsign book
			if ((args[0].matches("u(nsign)?"))
					&& CommandPermissions.UNSIGN.checkPermission(p)) {
				unsign(p);
				return true;
			}
			return invalidCommand(sender);
		}

		// command: /book <e(xport)|s(ave)> <filename> - attempt to save book in hand to file
		if (args[0].matches("e(xport)?|s(save)?") && CommandPermissions.EXPORT.checkPermission(p)) {
			export(p, args);
			return true;
		}

		// command: /book <i(mport)|f(ile)|l(oad)> <file> - attempt to import a locally saved book
		if (args[0].matches("f(ile)?|l(oad)?|i(mport)?") && CommandPermissions.IMPORT.checkPermission(p)) {
			importLocal(p, args);
			return true;
		}

		// command: /book u(rl) <url> - attempt to import a book from a remote location
		if (args[0].matches("u(rl)?") && CommandPermissions.IMPORT.checkPermission(p)) {
			importRemote(p, args);
			return true;
		}

		// command: /book d(elete) <filename> - attempt to
		// delete file
		if (args[0].matches("d(elete)?") && CommandPermissions.DELETE.checkPermission(p)) {
			if (!args[1].contains(".")) {
				args[1] += ".book";
			}
			new File(plugin.getDataFolder(), "/SavedBooks/" + args[1]).delete();
			p.sendMessage(plugin.getMessages().get("SUCCESS_DELETE").replace("<file.name>", args[1]));
			return true;
		}

		// command: /book t(itle) <args> - attempt to change title
		// with additional args. Include spaces.
		if (args[0].matches("t(itle)?") && CommandPermissions.TITLE.checkPermission(p)) {
			title(p, args);
			return true;
		}

		// command: /book addpage <number> (optional text) - add a
		// page to a book and quill
		if (args[0].equals("addpage") && CommandPermissions.EDIT.checkPermission(p)) {
			if (plugin.getFunctions().insertPageAt(p, args[1], StringUtils.join(args, ' ', 2, args.length - 1)))
				p.sendMessage(plugin.getMessages().get("SUCCESS_EDIT_ADDPAGE"));
			return true;
		}

		// command: /book delpage <number> - remove a page from a
		// book and quill
		if (args[0].equals("delpage")) {
			if (CommandPermissions.EDIT.checkPermission(p)) {
				if (plugin.getFunctions().deletePageAt(p, args[1]))
					p.sendMessage(plugin.getMessages().get("SUCCESS_EDIT_DELPAGE"));
				return true;
			}
		}

		// command: /book a(uthor) <args> - attempt to change author
		// with additional args. Include spaces.
		if (args[0].matches("a(uthor)?") && CommandPermissions.AUTHOR.checkPermission(p)) {
			author(p, args);
			return true;
		}

		return invalidCommand(sender);
	}

	private void title(Player p, String[] args) {
		if (!p.getInventory().getItemInMainHand().getType().equals(Material.WRITTEN_BOOK)) {
			p.sendMessage(plugin.getMessages().get("FAILURE_COMMAND_NEEDBOOK"));
			return;
		}
		if (plugin.getFunctions().isAuthor(p, ((BookMeta)p.getInventory().getItemInMainHand().getItemMeta()).getAuthor())
				|| p.hasPermission("booksuite.command.title.other")) {
			BookMeta bm = (BookMeta) p.getInventory().getItemInMainHand().getItemMeta();
			bm.setTitle(plugin.getFunctions().addColor(StringUtils.join(args, ' ', 1, args.length)));
			p.getInventory().getItemInMainHand().setItemMeta(bm);
			p.sendMessage(plugin.getMessages().get("SUCCESS_TITLE"));
		} else {
			p.sendMessage(plugin.getMessages().get("FAILURE_PERMISSION_TITLE_OTHER"));
		}
	}

	private void author(Player p, String[] args) {
		if (!p.getInventory().getItemInMainHand().getType().equals(Material.WRITTEN_BOOK)) {
			p.sendMessage(plugin.getMessages().get("FAILURE_COMMAND_NEEDBOOK"));
			return;
		}
		BookMeta bm = (BookMeta) p.getInventory().getItemInMainHand().getItemMeta();
		bm.setAuthor(plugin.getFunctions().addColor(StringUtils.join(args, ' ', 1, args.length)));
		p.getInventory().getItemInMainHand().setItemMeta(bm);
	}

	private void unsign(Player p) {
		if (!p.getInventory().getItemInMainHand().getType().equals(Material.WRITABLE_BOOK)
				&& !p.getInventory().getItemInMainHand().getType().equals(Material.WRITTEN_BOOK)) {
			p.sendMessage(plugin.getMessages().get("FAILURE_COMMAND_NEEDEITHER"));
			return;
		}
		if (plugin.getFunctions().isAuthor(p, ((BookMeta)p.getInventory().getItemInMainHand().getItemMeta()).getAuthor())
				|| p.hasPermission("booksuite.command.unsign.other")) {
			plugin.getFunctions().unsign(p);
			p.sendMessage(plugin.getMessages().get("SUCCESS_UNSIGN"));
			return;
		}
		p.sendMessage(plugin.getMessages().get("FAILURE_PERMISSION_UNSIGN_OTHER"));
	}

	private void reload(CommandSender sender) {
		plugin.reloadConfig();

		plugin.onDisable();
		plugin.onEnable();

		sender.sendMessage(plugin.getMessages().get("SUCCESS_RELOAD").replace("<plugin.version>", plugin.getDescription().getVersion()));
	}

	private void copyItem(Player p, String[] args) {
		int copies;
		if (args.length >= 2) {
			try {
				copies = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				p.sendMessage(plugin.getMessages().get("FAILURE_COPY_INVALID_NUMBER").replace("<number>", args[1]));
				copies = 1;
			}
		} else {
			copies = 1;
		}

		ItemStack is = p.getInventory().getItemInMainHand();
		if (is.getType().equals(Material.MAP)) {
			plugin.getFunctions().copy(p, copies);
			p.sendMessage(plugin.getMessages().get("SUCCESS_COPY"));
			return;
		}
		if (!is.hasItemMeta() || is.getItemMeta() == null) {
			p.sendMessage(plugin.getMessages().get("FAILURE_COPY_UNCOPIABLE"));
			return;
		}
		if (is.getType().equals(Material.WRITTEN_BOOK) || is.getType().equals(Material.WRITABLE_BOOK)) {
			BookMeta bm = (BookMeta) is.getItemMeta();
			if (plugin.getFunctions().checkCommandCopyPermission(p, bm.getAuthor())) {
				plugin.getFunctions().copy(p, copies);
				p.sendMessage(plugin.getMessages().get("SUCCESS_COPY"));
			}
			return;
		}
		p.sendMessage(plugin.getMessages().get("FAILURE_COPY_UNCOPIABLE"));
	}

	private void export(Player p, String[] args) {
		if (!p.getInventory().getItemInMainHand().getType().equals(Material.WRITTEN_BOOK)) {
			p.sendMessage(plugin.getMessages().get("FAILURE_COMMAND_NEEDBOOK"));
			return;
		}
		BookMeta bm = (BookMeta) p.getInventory().getItemInMainHand().getItemMeta();
		if (plugin.getFileManager().makeFileFromBookMeta(bm, plugin.getDataFolder() + "/SavedBooks/", args[1])) {
			p.sendMessage(plugin.getMessages().get("SUCCESS_EXPORT").replace("<book.savename>", args[1]));
			return;
		}
		p.sendMessage(plugin.getMessages().get("FAILURE_FILE_EXISTANT"));
		if (!p.hasPermission("booksuite.command.delete")) {
			return;
		}
		p.sendMessage(plugin.getMessages().get("OVERWRITE_FILE").replace("<book.savename>", args[1]));
		overwritable.put(p.getName(), args[1]);
		syncOverwriteTimer(p);
	}

	private void overwrite(Player p, String[] args) {
		if (!p.getInventory().getItemInMainHand().getType().equals(Material.WRITTEN_BOOK)) {
			p.sendMessage(plugin.getMessages().get("FAILURE_COMMAND_NEEDBOOK"));
			return;
		}
		if (overwritable.containsKey(p.getName())) {
			new File(plugin.getDataFolder(), "/SavedBooks/" + overwritable.get(p.getName())).delete();
			if (plugin.getFileManager().makeFileFromBookMeta((BookMeta) p.getInventory().getItemInMainHand().getItemMeta(),
					plugin.getDataFolder() + "/SavedBooks/", overwritable.get(p.getName()))) {
				p.sendMessage(plugin.getMessages().get("SUCCESS_EXPORT")
						.replace("<book.savename>", overwritable.get(p.getName())));
			}
			overwritable.remove(p.getName());
			return;
		}
		if (args.length != 2) {
			p.sendMessage(plugin.getMessages().get("FAILURE_OVERWRITE"));
			return;
		}
		if (!new File(plugin.getDataFolder(), "/SavedBooks/" + args[1]).delete()) {
			p.sendMessage(plugin.getMessages().get("OVERWRITE_WARN"));
		}
		if (plugin.getFileManager().makeFileFromBookMeta((BookMeta) p.getInventory().getItemInMainHand().getItemMeta(),
				plugin.getDataFolder() + "/SavedBooks/", args[1])) {
			p.sendMessage(plugin.getMessages().get("SUCCESS_EXPORT"));
		}
	}

	private void importLocal(Player p, String[] args) {
		ItemStack newbook = new ItemStack(Material.WRITTEN_BOOK, 1);
		File directory = new File(plugin.getDataFolder(), "SavedBooks");
		if (!directory.exists()) {
			p.sendMessage(plugin.getMessages().get("FAILURE_FILE_NONEXISTANT"));
			return;
		}
		File bookFile = new File(directory, args[1].contains(".") ? args[1] : args[1] + ".book");
		if (!bookFile.exists()) {
			p.sendMessage(plugin.getMessages().get("FAILURE_FILE_NONEXISTANT"));
			return;
		}
		newbook.setItemMeta(plugin.getFileManager().makeBookMetaFromText(p,
				plugin.getFileManager().getFileData(bookFile), false));
		if (!newbook.hasItemMeta() || newbook.getItemMeta() == null) {
			p.sendMessage(plugin.getMessages().get("FAILURE_FILE_NONEXISTANT"));
			return;
		}
		if (plugin.getFunctions().canObtainBook(p)) {
			p.getInventory().addItem(newbook);
			p.sendMessage(plugin.getMessages().get("SUCCESS_IMPORT").replace("<book.savename>", args[1]));
		}
	}

	private void importRemote(Player p, String[] args) {
		if (plugin.getFunctions().canObtainBook(p)) {
			asyncBookImport(p, args[1]);
			p.sendMessage(plugin.getMessages().get("SUCCESS_IMPORT_INITIATED"));
		}
	}

	private void give(CommandSender s, String[] args) {
		Player recipient = Bukkit.getPlayer(args[1]);
		if (recipient == null) {
			s.sendMessage(plugin.getMessages().get("FAILURE_PLAYER"));
			return;
		}
		ItemStack newbook = new ItemStack(Material.WRITTEN_BOOK, 1);
		File directory = new File(plugin.getDataFolder(), "SavedBooks");
		if (!directory.exists()) {
			s.sendMessage(plugin.getMessages().get("FAILURE_FILE_NONEXISTANT"));
			return;
		}
		File bookFile = new File(directory, args[2].contains(".") ? args[2] : args[2] + ".book");
		if (!bookFile.exists()) {
			s.sendMessage(plugin.getMessages().get("FAILURE_FILE_NONEXISTANT"));
			return;
		}
		newbook.setItemMeta(plugin.getFileManager().makeBookMetaFromText(s,
				plugin.getFileManager().getFileData(bookFile), false));
		if (!newbook.hasItemMeta() || newbook.getItemMeta() == null) {
			s.sendMessage(plugin.getMessages().get("FAILURE_FILE_NONEXISTANT"));
			return;
		}
		if (!plugin.getFunctions().canObtainBook(s)) {
			return;
		}
		recipient.getInventory().addItem(newbook);
		s.sendMessage(plugin.getMessages().get("SUCCESS_GIVE").replace("<book.savename>", args[2])
				.replace("<player>", args[1]));
		recipient.sendMessage(plugin.getMessages().get("SUCCESS_GIVE_RECIEVE")
				.replace("<book.savename>", args[2]).replace("<sender>", s.getName())
				.replace("<book.title>", ((BookMeta) newbook.getItemMeta()).getTitle()));
	}

	private void usage(Player p, String[] args) {
		if (listPermittedCommands(p).length() <= 0) {
			return;
		}
		if (args.length == 1) {
			p.sendMessage(plugin.getMessages().get("USAGE"));
			p.sendMessage(plugin.getMessages().get("USAGE_TOPICS") + listPermittedCommands(p));
			return;
		}
		boolean failure = false;
		StringBuilder sb1 = new StringBuilder();
		for (String s : args) {
			s = s.replaceAll("\\W\\z", "");
			if (!s.equals(args[0])) {
				try {
					CommandPermissions cdp = CommandPermissions.uValueOf(s);
					if (cdp.checkPermission(p)) {
						switch (cdp) {
						case EDIT:
							p.sendMessage(plugin.getMessages().get("USAGE_EDIT_ADDPAGE")
									+ plugin.getMessages().get("USAGE_EDIT_ADDPAGE_EXPLANATION"));
							p.sendMessage(plugin.getMessages().get("USAGE_EXAMPLE")
									+ plugin.getMessages().get("USAGE_EDIT_ADDPAGE_EXAMPLE"));
							p.sendMessage(plugin.getMessages().get("USAGE_EDIT_DELPAGE")
									+ plugin.getMessages().get("USAGE_EDIT_DELPAGE_EXPLANATION"));
							p.sendMessage(plugin.getMessages().get("USAGE_EXAMPLE")
									+ plugin.getMessages().get("USAGE_EDIT_DELPAGE_EXAMPLE"));
							break;
						default:
							p.sendMessage(plugin.getMessages().get("USAGE_" + cdp.toString())
									+ plugin.getMessages().get("USAGE_" + cdp.toString() + "_EXPLANATION"));
							p.sendMessage(plugin.getMessages().get("USAGE_EXAMPLE")
									+ plugin.getMessages().get("USAGE_" + cdp.toString() + "_EXAMPLE"));
							break;
						}
					} else {
						failure = true;
						sb1.append(s).append(", ");
					}
				} catch (IllegalArgumentException e) {
					if ((s.equalsIgnoreCase("press") || s.equalsIgnoreCase("printingpress"))
							&& p.hasPermission("booksuite.copy.self")) {
						p.sendMessage(plugin.getMessages().get("USAGE_PRESS"));
						if (p.hasPermission("booksuite.copy.createpress"))
							p.sendMessage(plugin.getMessages().get("USAGE_PRESS_CREATE"));
						p.sendMessage(plugin.getMessages().get("USAGE_PRESS_COPIABLES"));
					} else if ((s.equalsIgnoreCase("erase") || s.equalsIgnoreCase("eraser"))
							&& p.hasPermission("booksuite.block.erase")) {
						p.sendMessage(plugin.getMessages().get("USAGE_ERASER"));
						if (!p.hasPermission("booksuite.block.erase.free"))
							p.sendMessage(plugin.getMessages().get("USAGE_ERASER_WATER"));
					} else {
						failure = true;
						sb1.append(s).append(", ");
					}
				}
			}
		}

		if (failure) {
			p.sendMessage(plugin.getMessages().get("UNKNOWN_TOPIC") + sb1.substring(0, sb1.length() - 2));
			p.sendMessage(plugin.getMessages().get("USAGE_TOPICS"));
			p.sendMessage(ChatColor.DARK_RED + listPermittedCommands(p));
		}
	}

	private String listPermittedCommands(CommandSender s) {
		StringBuilder sb = new StringBuilder();

		if (s.hasPermission("booksuite.copy.self"))
			sb.append(ChatColor.AQUA.toString()).append("printingpress").append(ChatColor.DARK_GREEN.toString()).append(", ");
		if (s.hasPermission("booksuite.block.erase"))
			sb.append(ChatColor.AQUA.toString()).append("eraser").append(ChatColor.DARK_GREEN.toString()).append(", ");
		for (CommandPermissions i : CommandPermissions.values()) {
			if (i.checkPermission(s))
				sb.append(ChatColor.AQUA).append(i.lName()).append(ChatColor.DARK_GREEN).append(", ");
		}

		return sb.substring(0, sb.length() - 2);
	}

	private boolean invalidCommand(CommandSender sender) {
		if (listPermittedCommands(sender).length() > 0) {
			sender.sendMessage(plugin.getMessages().get("VERSION").replaceAll("<plugin.version>", plugin.getDescription().getVersion()));
			sender.sendMessage(plugin.getMessages().get("USAGE_HELP"));
			sender.sendMessage(plugin.getMessages().get("USAGE_TOPICS") + listPermittedCommands(sender));
			return true;
		}
		sender.sendMessage(plugin.getMessages().get("UNKNOWN_COMMAND"));
		return true;
	}

	private void asyncBookImport(final Player p, final String s) {
		new BukkitRunnable() {
			@Override
			public void run() {
				BookMeta bm;
				StringBuilder sb = new StringBuilder();
				try {
					URL url = new URL(s);
					Scanner urlInput = new Scanner(url.openStream());
					while (urlInput.hasNextLine()) {
						sb.append(urlInput.nextLine()).append('\n');
					}
					urlInput.close();
				} catch (Exception e) {}
				bm = plugin.getFileManager().makeBookMetaFromText(p, sb.toString(), true);
				syncBookImport(p, bm);
			}
		}.runTaskAsynchronously(plugin);
	}

	private void syncBookImport(final Player p, final BookMeta bm) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ItemStack is = new ItemStack(Material.WRITTEN_BOOK);
				if (bm.hasPages()) {
					is.setItemMeta(bm);
					if (p.getInventory().firstEmpty() != -1) {
						p.getInventory().addItem(is);
					} else {
						p.getWorld().dropItem(p.getLocation(), is).setPickupDelay(0);
					}
				} else {
					p.sendMessage(plugin.getMessages().get("FAILURE_IMPORT_URL"));
					if (p.getInventory().firstEmpty() > 0) {
						p.getInventory().addItem(new ItemStack(Material.INK_SAC, 1));
						p.getInventory().addItem(new ItemStack(Material.BOOK, 1));
					} else {
						p.getWorld().dropItem(p.getLocation(),
								new ItemStack(Material.INK_SAC, 1)).setPickupDelay(0);
						p.getWorld().dropItem(p.getLocation(),
								new ItemStack(Material.BOOK, 1)).setPickupDelay(0);
					}
				}
			}
		}.runTask(plugin);
	}

	private void syncOverwriteTimer(final Player p) {
		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			if (overwritable.containsKey(p.getName())) {
				overwritable.remove(p.getName());
				p.sendMessage(plugin.getMessages().get("FAILURE_OVERWRITE_TIME"));
			}
		}, 200L);
	}
}
