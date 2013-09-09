package com.github.Jikoo.BookSuite.module;

public interface ModuleManager {
	
	/**
	 * 
	 * @return the module that the instance of this class accesses
	 */
	public BookSuiteModule getManagedModule();
}
