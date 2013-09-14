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

import java.util.HashMap;
import java.util.Map;

import org.bukkit.event.Event;

import com.github.Jikoo.BookSuite.struct.cache.Cache;

public class ModuleManagementSystem {

	Cache<String, BookSuiteModule> modules = new Cache<String, BookSuiteModule>(10);
	
	public BookSuiteModule getModuleByName(String s)
	{
		return modules.get(s);
	}
	
	public void disableModule(String s)
	{
		modules.get(s).disable();
	}
	
	public void enableModule(String s)
	{
		modules.get(s).enable();
	}
	
	public void performCancelableAction(Event e)
	{
		for (BookSuiteModule bsm : modules) {
			if (bsm.isEnabled() && bsm.isTriggeredByEvent(event)) {
				event.setCancelled(true);
			}
			if (event.isCancelled()) {
				return;
			}
		}
	}
	
	
	
	
}
