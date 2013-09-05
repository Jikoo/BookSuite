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

import java.io.File;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author Jikoo
 * 
 */
public enum Msgs {
	FAILURE_COPY_BOOK,
	FAILURE_COPY_BOTH,
	FAILURE_COPY_INK,
	FAILURE_COPY_MAP,
	FAILURE_COPY_UNCOPIABLE,
	FAILURE_EDIT_INVALIDNUMBER,
	FAILURE_EDIT_NOBAQ,
	FAILURE_LIST_NOBOOKS,
	FAILURE_PERMISSION_COPY,
	FAILURE_PERMISSION_COPY_OTHER,
	FAILURE_SPACE,
	FAILURE_USAGE,
	SUCCESS_COPY,
	SUCCESS_LIST_PUBLIC,
	SUCCESS_LIST_PRIVATE,
	USAGE_EDIT_ADDPAGE,
	USAGE_EDIT_DELPAGE,
	VERSION;

	private YamlConfiguration strings;

	Msgs() {
		String defaultLocation = "plugins" + File.pathSeparatorChar + "BookSuite"
				+ File.pathSeparatorChar;
		BookSuite.getInstance().saveResource("strings.yml", false);
		File f = new File(defaultLocation, "strings.yml");
		if (f.exists()) {
			strings = YamlConfiguration.loadConfiguration(f);
		} else {
			strings = YamlConfiguration.loadConfiguration(BookSuite.getInstance().getResource(
					"strings.yml"));
		}
	}

	public String getMessage() {
		String msg = BookSuite.getInstance().functions.parseBML(strings.getString(this.name()
				.toLowerCase()));
		return msg.equals("null") ? null : msg;
	}

	public String toString() {
		return BookSuite.getInstance().functions.parseBML(YamlConfiguration.loadConfiguration(
				BookSuite.getInstance().getResource("strings.yml")).getString(
				this.name().toLowerCase()));
	}
}
