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
