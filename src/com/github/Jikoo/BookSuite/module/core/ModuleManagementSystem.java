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
package com.github.Jikoo.BookSuite.module.core;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

import com.github.Jikoo.BookSuite.struct.cache.Cache;

public class ModuleManagementSystem {

	private Cache<String, BookSuiteModule> modules = new Cache<String, BookSuiteModule>(
			10);
	public boolean depenancyUpdated = true;

	/**
	 * 
	 * @param s
	 *            the name of the module
	 * @return the module with the provided name
	 */
	public BookSuiteModule getModuleByName(String s) {
		return modules.get(s);
	}

	/**
	 * 
	 * @param s
	 *            the name of the module to disable
	 */
	public void disableModule(String s) {
		modules.get(s).disable();
		depenancyUpdated = true;
	}

	/**
	 * 
	 * @param s
	 *            the name of the module to enable
	 */
	public void enableModule(String s) {
		modules.get(s).enable();
		depenancyUpdated = true;
	}

	/**
	 * 
	 * @param m
	 *            the module to add
	 * @param s
	 *            the name of the module to add
	 */
	public void addModule(BookSuiteModule m, String s) {
		this.modules.insert(s, m);
		depenancyUpdated = true;
	}

	/**
	 * 
	 * @param e
	 *            the event that is being checked
	 * @throws ClassCastException
	 *             this is thrown if the event is not able to be canceled
	 */
	public void performCancelableAction(Event e) throws ClassCastException {

		for (BookSuiteModule bsm : modules) {

			if (bsm.isEnabled() && bsm.isTriggeredByEvent(e)) {
				if (e instanceof Cancellable) {
					((Cancellable) e).setCancelled(true);
				} else {
					throw new ClassCastException(
							"you tried to cancel an uncancelable event");
				}
			}
			if (e instanceof Cancellable) {
				if (((Cancellable) e).isCancelled()) {
					return;
				}
			} else {
				throw new ClassCastException(
						"you tried to cancel an uncancelable event");
			}
		}
	}

	/**
	 * 
	 * @param e
	 *            the event to be checked
	 */
	public void performNonCancelableAction(Event e) {
		for (BookSuiteModule bsm : modules) {
			if (bsm.isEnabled()) {
				bsm.isTriggeredByEvent(e);
			}
		}
	}
}
