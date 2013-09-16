package com.github.Jikoo.BookSuite.press;

import com.github.Jikoo.BookSuite.module.core.BookSuiteModule;
import com.github.Jikoo.BookSuite.module.core.DirectModuleManager;

public class JohannesGutenberg implements DirectModuleManager {

	public BookSuiteModule getManagedModule(boolean b) {
		PrintingCompany pc = PrintingCompany.getInstance();
		if (!b) {
			pc.disable();
		}
		return pc;
	}

}
