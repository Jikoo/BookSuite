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

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.Scanner;

class FileManager {

	private final BookSuite plugin;

	FileManager(BookSuite plugin) {
		this.plugin = plugin;
	}

	/**
	 * Makes a BookMeta from text. Text is read from a plaintext File file
	 * stored in the directory location. If the file is read from a URL,
	 * additional regex is applied to prevent special characters appearing
	 * incorrectly.
	 *
	 * @param s the CommandSender attempting to load a book
	 * @param fileData the BookMeta in Book Markdown Language
	 * @param isURL the type of import
	 *
	 * @return the BookMeta created.
	 */
	BookMeta makeBookMetaFromText(CommandSender s, String fileData, boolean isURL) {
		BookMeta text = (BookMeta) new ItemStack(Material.WRITTEN_BOOK, 1).getItemMeta();
		boolean isBookText = !isURL;
		String page = "";
		for (String line : fileData.split("\n")) {

			// pastebin support section
			if (isURL) {
				line = line.replaceAll("(<li class=\").*(\">)", "").replace("</li>", "");
				line = line.replaceAll("(<div class=\").*(\">)", "").replace("</div>", "");
				line = line.replace("&lt;", "<").replace("&gt;", ">");
				line = line.replace("&nbsp", "<n>");
			}

			if (line.contains("<book>")) {
				isBookText = true;
				line = line.replace("<book>", "");
			}
			if (isBookText) {
				if (line.contains("</book>")) {
					break;
				}
				if (line.length() >= 2 && line.substring(0, 2).equals("//")) {
					// do nothing, this line is a book comment
					continue;
				}
				if (line.contains("<author>")
						&& (!isURL || s.hasPermission("booksuite.command.import.other"))) {
					text.setAuthor(line.replace("<author>", "").replace("</author>", ""));
				} else if (line.contains("<title>")) {
					text.setTitle(line.replace("<title>", "").replace("</title>", "").replace("<br>", ""));
				} else if (line.contains("<page>")) {
					page = "";
				} else if (line.contains("</page>")) {
					text.addPage(plugin.getFunctions().parseBML(page));
				} else {
					page += line + "<n>";
				}
			}
		}
		if (!text.hasAuthor())
			text.setAuthor(s.getName());
		return text;
	}

	String getFileData(File file) {
		if (!file.exists()) {
			throw new RuntimeException("Requested file does not exist: " + file.getAbsolutePath() + File.pathSeparator + file.getName());
		}
		Scanner s = null;
		StringBuilder sb = new StringBuilder();
		try {
			s = new Scanner(file);
			while (s.hasNextLine()) {
				sb.append(s.nextLine()).append('\n');
			}
			s.close();
			return sb.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (s != null) {
				s.close();
			}
		}
	}

	/**
	 * Makes a plaintext File filename in directory directory from the contents
	 * of BookMeta bm.
	 *
	 * @param bm the BookMeta
	 * @param directory the directory to write to
	 * @param filename the File to write
	 *
	 * @return true, if successful
	 */
	boolean makeFileFromBookMeta(BookMeta bm, String directory,
								 String filename) {

		try {
			File bookLocation = new File(directory);
			if (!bookLocation.exists()) {
				bookLocation.mkdirs();
			}
			if (!filename.contains(".")) {
				filename += ".book";
			}
			File bookFile = new File(bookLocation, filename);
			if (!bookFile.exists()) {
				bookFile.createNewFile();
			} else {
				return false;
			}
			FileWriter file = new FileWriter(bookFile);
			file.write("<book>\n");
			file.append("<author>").append(bm.getAuthor()).append("</author>\n");
			file.append("<title>").append(bm.getTitle()).append("</title>\n");
			for (int i = 1; i <= bm.getPageCount(); i++) {
				file.append("<page>\n").append(bm.getPage(i)).append("\n</page>\n");
			}
			file.append("</book>");
			file.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return true;
		}
	}

}
