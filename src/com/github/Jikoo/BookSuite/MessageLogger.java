/*******************************************************************************
 * Copyright (c) 2013 Adam Gunn.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn- initial API and implementation
 *     Ted Meyer - some help
 ******************************************************************************/
package com.github.Jikoo.BookSuite;

import org.bukkit.Bukkit;

public class MessageLogger {
	// TODO strings.yml + hella configurability
	public static void info(String msg) {
		Bukkit.getConsoleSender().sendMessage("[BookSuite] " + msg);
	}
}
