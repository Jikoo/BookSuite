/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn
 ******************************************************************************/
package com.github.jikoo.booksuite;

import org.bukkit.Bukkit;

public class BSLogger {

	/**
	 * A small utility to replace BookSuite.getInstance().getLogger.info()
	 * <p>
	 * Supports color in console.
	 * 
	 * @param msg the message to send
	 */
	public static void info(String msg) {
		Bukkit.getConsoleSender().sendMessage("[BookSuite] " + msg);
	}

	public static void warn(String msg) {
		Bukkit.getLogger().warning("[BookSuite] " + msg);
	}

	public static void severe(String msg) {
		Bukkit.getLogger().severe("[BookSuite] " + msg);
	}

	public static void err(Exception e) {
		warn(assemble(e));
	}

	public static void criticalErr(Exception e) {
		severe(assemble(e));
	}

	private static String assemble(Exception e) {
		StringBuilder trace = new StringBuilder("Error report:\n");
		trace.append(e.toString());
		for (StackTraceElement ste : e.getStackTrace()) {
			trace.append("\n\tat ").append(ste.toString());
		}
		if (e.getCause() != null) {
			trace.append("\nCaused by: " + e.getCause().toString());
			for (StackTraceElement ste : e.getCause().getStackTrace()) {
				trace.append("\n\tat " + ste.toString());
			}
		}
		trace.append("\nEnd of error report.");
		return trace.toString();
	}
}
