package com.github.Jikoo.BookSuite.copy;

import com.github.Jikoo.BookSuite.mail.PostalService;
import com.github.Jikoo.BookSuite.module.BookSuiteModule;
import com.github.Jikoo.BookSuite.module.ModuleManager;

public class JohannesGutenberg implements ModuleManager{
	
	@Override
	public BookSuiteModule getManagedModule() {
		return PostalService.getInstance();
	}
	
}
