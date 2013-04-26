package com.github.Jikoo.BookSuite.rules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.github.Jikoo.BookSuite.BookSuite;

public class Rules implements CommandExecutor {

	BookSuite plugin;

	public Rules(BookSuite plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String Label, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}

}
