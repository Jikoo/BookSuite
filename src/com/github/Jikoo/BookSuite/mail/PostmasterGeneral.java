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
package com.github.Jikoo.BookSuite.mail;

import com.github.Jikoo.BookSuite.module.BookSuiteModule;
import com.github.Jikoo.BookSuite.module.ModuleManager;

public class PostmasterGeneral implements ModuleManager{

	@Override
	public BookSuiteModule getManagedModule() {
		return PostalService.getInstance();
	}

}
