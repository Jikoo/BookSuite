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

public interface BookSuiteModule {

	/**
	 * 
	 * if this module is intended to handle the event, this will handle it, then
	 * return true, otherwise, return false
	 * 
	 * @param e
	 *            the event
	 * @return whether or not this module has handled the event
	 */
	public boolean isTriggeredByEvent(Event e);

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
	public boolean isTriggeringCommand(Command c, String[] args,
			CommandSender sender, String label);

	/**
	 * 
	 * @return whether this pluggin is disabled or not
	 */
	public boolean isEnabled();

	/**
	 * 
	 * @return the error status of this method. 0 mean success
	 */
	public int disable();

	/**
	 * 
	 * @return whether the module was enabled (if it already was, false is returned)
	 */
	public boolean enable();
	
	public String getName();
}
