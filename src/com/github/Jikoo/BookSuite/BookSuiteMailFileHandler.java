package com.github.Jikoo.BookSuite;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BookSuiteMailFileHandler {
	
	public static String getSentMails(String username) throws IOException{
		File f;
		try {
			f = getUserIndexFile(username);
		} catch (Exception e){
			createIndex(username);
			f = getUserIndexFile(username);
		}
		List<String> s = new ArrayList<String>();
		Scanner reader = new Scanner(f);
		while(reader.hasNext()){
			s.add(reader.nextLine());
		}
		return null; //temporary
	}
	
	public static void createIndex(String username) throws IOException{
		File f = new File("/BookSuiteMail/"+username);
		f.mkdir();
		File index = new File("/bookSuiteMail/"+username+"/index.bsm");
		index.createNewFile();
	}
	
	public static File getUserIndexFile(String u) throws IOException {
		if (!(new File("/BookSuiteMail/"+u).exists()))
			createIndex(u);
		return new File("/BookSuiteMail/"+u+"/index.bsm");
	}
	
	
	
	
	
	
	
	
	
	
	
	public static void setup(){
		File f = new File("/BookSuiteMail");
		if (!f.exists())f.mkdir();
	}
}
