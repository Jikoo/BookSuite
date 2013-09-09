package com.github.Jikoo.BookSuite.module;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.github.Jikoo.BookSuite.BookSuite;
import com.github.Jikoo.BookSuite.MainListener;
import com.github.Jikoo.BookSuite.io.json.JsonValue;

public class ReflectiveModuleInstantiatier {

	public static void loadModules(MainListener ml) {
		ClassLoader classLoader = ReflectiveModuleInstantiatier.class
				.getClassLoader();
		JsonValue modules;
		try {
			modules = JsonValue.getJsonValue(readFile(BookSuite.getInstance()
					.getDataFolder() + "/modules.json"));
			for (JsonValue jv : modules) {
				Class<?> aClass = classLoader.loadClass(jv.get("classpath")
						.valueOf());
				ModuleManager ob = (ModuleManager) aClass.newInstance();
				ml.addModule(ob.getManagedModule());
			}
		} catch (Exception e) {
			e.printStackTrace();
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
