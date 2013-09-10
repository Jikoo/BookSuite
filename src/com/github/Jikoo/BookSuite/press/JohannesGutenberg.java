package com.github.Jikoo.BookSuite.press;

import com.github.Jikoo.BookSuite.module.BookSuiteModule;
import com.github.Jikoo.BookSuite.module.ModuleManager;

public class JohannesGutenberg implements ModuleManager{
	
	@Override
	public BookSuiteModule getManagedModule() {
		return PrintingCompany.getInstance();
	}
	
}
