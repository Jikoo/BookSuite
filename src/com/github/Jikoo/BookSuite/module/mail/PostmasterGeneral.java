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
package com.github.Jikoo.BookSuite.module.mail;

import com.github.Jikoo.BookSuite.module.core.BookSuiteModule;
import com.github.Jikoo.BookSuite.module.core.DirectModuleManager;

public class PostmasterGeneral implements DirectModuleManager {

	public BookSuiteModule getManagedModule(boolean b) {
		PostalService p = PostalService.getInstance();
		if (!b) {
			p.disable();
		}
		return p;
	}

}
