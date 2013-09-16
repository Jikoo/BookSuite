/*******************************************************************************
 * Copyright (c) 2013 Ted Meyer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn - plugin surrounding libraries
 ******************************************************************************/
package com.github.Jikoo.BookSuite.module.core;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.github.Jikoo.BookSuite.BSLogger;
import com.github.Jikoo.BookSuite.BookSuite;
import com.github.Jikoo.BookSuite.struct.json.JsonArray;
import com.github.Jikoo.BookSuite.struct.json.JsonValue;

public class ReflectiveModuleInstantiatier {

	public static void loadModules(ModuleManagementSystem ml) {
		ClassLoader classLoader = ReflectiveModuleInstantiatier.class
				.getClassLoader();
		JsonValue modules;
		try {
			BookSuite.getInstance().saveResource("modules.json", false);
			modules = JsonValue.getJsonValue(readFile(BookSuite.getInstance()
					.getDataFolder() + "/modules.json"));
			for (JsonValue jv : modules) {
				Class<?> aClass = classLoader.loadClass(jv.get("classpath")
						.valueOf());
				DirectModuleManager ob = (DirectModuleManager) aClass
						.newInstance();
				String name = jv.get("name").valueOf();
				boolean d = "enabled".equals(jv.get("default").valueOf());
				BookSuiteModule m = ob.getManagedModule(d);
				JsonArray dependencies = (JsonArray) jv.get("dependencies");
				for (JsonValue dep : dependencies) {
					m.addDependancy(dep.valueOf());
				}
				String msg = new StringBuilder("The module").append(name)
						.append(" was loaded in state: ")
						.append(d ? "enabled" : "disabled").toString();

				ml.addModule(m, name);
				BSLogger.info(msg);

			}
		} catch (Exception e) {
			BSLogger.criticalErr(e);
		}

	}

	static String readFile(String path) throws IOException {
		Scanner reader = new Scanner(new File(path));
		StringBuilder sb = new StringBuilder();
		while (reader.hasNext()) {
			sb.append(reader.next());
		}
		reader.close();
		return sb.toString();
	}
}
