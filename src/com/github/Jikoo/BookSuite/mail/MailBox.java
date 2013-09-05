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

import java.util.LinkedList;
import java.util.List;

public class MailBox {
	private String user;
	private int maxSize;
	private List<BookMailWrapper> inventory = new LinkedList<BookMailWrapper>();
	
	public MailBox(String user)
	{
		this.user = user;
	}
	
	public void sendMail()
	{
		
	}
	
	public void getMail()
	{
		if (this.inventory.size() < this.maxSize)
		{
			PostalService.getInstance().distribute(user, this.maxSize-this.inventory.size());
		}
	}
}
