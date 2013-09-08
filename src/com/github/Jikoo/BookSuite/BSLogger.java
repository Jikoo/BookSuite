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
package com.github.Jikoo.BookSuite;

import org.bukkit.Bukkit;

public class BSLogger {
	/**
	 * A small function to replace Logger.info()
	 * <p>
	 * Supports color in console.
	 * 
	 * @param msg
	 *            the message to send
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

	public static void debugInfo(String msg) {
		if (!BookSuite.getInstance().getConfig().getBoolean("debug-mode")) {
			return;
		}
		Bukkit.getConsoleSender().sendMessage("[BookSuite Debug] " + msg);
	}

	public static void debugWarn(String msg) {
		if (!BookSuite.getInstance().getConfig().getBoolean("debug-mode")) {
			return;
		}
		Bukkit.getLogger().warning("[BookSuite Debug] " + msg);
	}

	public static void err(Exception e) {
		if (!BookSuite.getInstance().getConfig().getBoolean("debug-mode")) {
			return;
		}
		String trace = e.toString();
		for (StackTraceElement ste : e.getStackTrace()) {
			trace += "\n\tat " + ste.toString();
		}
		debugWarn("Error report:\n" + trace);
		debugWarn("End of error report.");
	}

	public static void criticalErr(Exception e) {
		String trace = e.toString();
		for (StackTraceElement ste : e.getStackTrace()) {
			trace += "\n\tat " + ste.toString();
		}
		severe("Error report:\n" + trace);
		severe("End of error report.");
	}
}
