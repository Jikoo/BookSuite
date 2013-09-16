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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

public abstract class BookSuiteModule {

	/**
	 * 
	 * if this module is intended to handle the event, this will handle it, then
	 * return true, otherwise, return false
	 * 
	 * @param e
	 *            the event
	 * @return whether or not this module has handled the event
	 */
	public abstract boolean isTriggeredByEvent(Event e);

	/**
	 * 
	 * @param c
	 *            the command to be tested for validity
	 * @param args
	 *            the arguments to the command
	 * @param sender
	 *            the entity (probably player) that sent the command
	 * @param label
	 *            the label of the command? (ask adam)
	 * 
	 * @return whether this module is capable of dealing with this command
	 *         effectively
	 */
	public abstract boolean isTriggeringCommand(Command c, String[] args,
			CommandSender sender, String label);

	/**
	 * 
	 * @return whether this pluggin is disabled or not
	 */
	public abstract boolean isEnabled();

	/**
	 * 
	 * @return the error status of this method. 0 mean success
	 */
	public abstract int disable();

	/**
	 * 
	 * @return whether the module was enabled (if it already was, false is
	 *         returned)
	 */
	public abstract boolean enable();

	public abstract String getName();

	ModuleDependencyChecker mdc;

	/**
	 * 
	 * @param mdc
	 *            the dependency checker that this module will use for itself
	 */
	public void addModuleDependencyChecker(ModuleDependencyChecker mdc) {
		this.mdc = mdc;
	}

	/**
	 * 
	 * @return whether the dependencies have been fulfilled
	 */
	public boolean dependenciesFulfilled() {
		return this.mdc == null || this.mdc.dependenciesFulfilled();
	}

	/**
	 * 
	 * @param moduleName
	 *            the name of the module to set as the dependency to this one
	 */
	public void addDependancy(String moduleName) {
		mdc.addDependency(moduleName);
	}
}
