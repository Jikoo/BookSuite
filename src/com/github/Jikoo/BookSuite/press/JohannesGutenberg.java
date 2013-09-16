package com.github.Jikoo.BookSuite.press;

import com.github.Jikoo.BookSuite.module.BookSuiteModule;
import com.github.Jikoo.BookSuite.module.DirectModuleManager;

public class JohannesGutenberg implements DirectModuleManager {

	public BookSuiteModule getManagedModule(boolean b) {
		PrintingCompany pc = PrintingCompany.getInstance();
		if (!b) {
			pc.disable();
		}
		return pc;
	}

}
