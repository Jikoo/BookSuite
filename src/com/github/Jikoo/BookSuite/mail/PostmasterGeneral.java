package com.github.Jikoo.BookSuite.mail;

import com.github.Jikoo.BookSuite.module.BookSuiteModule;
import com.github.Jikoo.BookSuite.module.ModuleManager;

public class PostmasterGeneral implements ModuleManager{

	@Override
	public BookSuiteModule getManagedModule() {
		return PostalService.getInstance();
	}

}
