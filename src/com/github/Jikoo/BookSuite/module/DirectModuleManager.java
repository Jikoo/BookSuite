/*******************************************************************************
 * Copyright (c) 2013 Ted Meyer
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Adam Gunn - plugin surrounding libraries
 ******************************************************************************/
package com.github.Jikoo.BookSuite.module;

public interface DirectModuleManager {

	/**
	 * 
	 * @return the module that the instance of this class accesses
	 */
	public BookSuiteModule getManagedModule(boolean enabled);
}
