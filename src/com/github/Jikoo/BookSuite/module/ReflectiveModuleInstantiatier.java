package com.github.Jikoo.BookSuite.module;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.github.Jikoo.BookSuite.MainListener;
import com.github.Jikoo.BookSuite.io.json.JsonArray;
import com.github.Jikoo.BookSuite.io.json.JsonException;
import com.github.Jikoo.BookSuite.io.json.JsonValue;


public class ReflectiveModuleInstantiatier {

	public static void loadModules(MainListener ml) throws IOException {
		ClassLoader classLoader = ReflectiveModuleInstantiatier.class
				.getClassLoader();
		JsonValue modules = JsonValue.getJsonValue(readFile("modules.json"));

		for(JsonValue jv : modules)
		{
			try {
				Class<?> aClass = classLoader.loadClass(jv.get("classpath").toString());
				ModuleManager ob = (ModuleManager) aClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static String readFile(String path) throws IOException {
		Scanner reader = new Scanner(new File(path));
		StringBuilder sb = new StringBuilder();
		while(reader.hasNext()){
			sb.append(reader.next());
		}
		reader.close();
		return sb.toString();
	}
	
	
	public static void main(String[] args) throws JsonException, IOException{
		JsonValue modules = JsonValue.getJsonValue(readFile("modules.json"));
		System.out.println(((JsonArray)modules).get(0));

		for(JsonValue jv : modules)
		{
			System.out.println(jv.get("classpath").toString());
		}
	}
}
