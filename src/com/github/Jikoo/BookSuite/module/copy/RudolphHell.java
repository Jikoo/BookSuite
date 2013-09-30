package com.github.Jikoo.BookSuite.module.copy;

import com.github.Jikoo.BookSuite.module.core.BookSuiteModule;
import com.github.Jikoo.BookSuite.module.core.DirectModuleManager;

//damn i have the most strange class names
public class RudolphHell implements DirectModuleManager{

	public BookSuiteModule getManagedModule(boolean enabled) {
		return Copier.getInstance();
	}
	
}
