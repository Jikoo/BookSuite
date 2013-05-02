package com.github.Jikoo.BookSuite;

import java.io.File;
import java.io.IOException;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.Jikoo.BookSuite.metrics.Metrics;
import com.github.Jikoo.BookSuite.permissions.PermissionsListener;
import com.github.Jikoo.BookSuite.rules.Rules;
import com.github.Jikoo.BookSuite.update.UpdateCheck;
import com.github.Jikoo.BookSuite.update.UpdateConfig;


public class BookSuite extends JavaPlugin implements Listener {
	String version = "3.2.0";
	public int currentFile = 11;
	public boolean hasUpdate;
	public String updateString;

	CommandExecutor originalRules;
	CommandExecutor originalQuestion;

	UpdateCheck update;
	PermissionsListener perms;
	Rules rules;
	Metrics metrics;

	public Functions functions;
	public FileManager filemanager;
	MainListener listener;
	CommandHandler command;
	MailExecutor mail;
	Alias alias;

	@Override
	public void onEnable() {
		getLogger().info("[BookSuite] Initializing.");
		
		saveDefaultConfig();
		
		if (new UpdateConfig(this).update())
			getLogger().warning("[BookSuite] Your configuration has been changed, please check it!");
		
		mail = new MailExecutor();
		functions = new Functions();
		filemanager = new FileManager();
		command = new CommandHandler(this);
		listener = new MainListener(this);
		rules = new Rules(this);
		
		alias = new Alias(this);
		alias.load();
		
		if (getConfig().getBoolean("update-check") || getConfig().getBoolean("allow-update-command"))
			update = new UpdateCheck(this);
		
		try {
			if (getConfig().getBoolean("use-inbuilt-permissions")) {
				getLogger().info("[BookSuite] Enabling inbuilt permissions.");
				perms = new PermissionsListener(this);
				perms.enable();
			}
			
			
			if (getConfig().getBoolean("enable-metrics")) {
				getLogger().info("[BookSuite] Enabling metrics.");
				try {
					metrics = new Metrics(this);
					metrics.start();
				} catch (IOException e) {
					getLogger().warning("[BookSuite] Error enabling metrics: " + e);
					e.printStackTrace();
					getLogger().warning("[BookSuite] End error report.");
					if (metrics != null) {
						metrics.disable();
						metrics = null;
					}
				}
			}
			
			
			if(getConfig().getBoolean("update-check")) {
				if(getConfig().getBoolean("login-update-check")) {
					getLogger().info("[BookSuite] Enabling login update check.");
					update.enableNotifications();
				}
				
				getLogger().info("[BookSuite] Starting update check...");
				
				update.asyncUpdateCheck(null, false);
			}
			
			
		} catch (Exception e) {
			getLogger().warning("[BookSuite] Error loading configuration: " + e);
			e.printStackTrace();
			getLogger().warning("[BookSuite] End error report.");
		}
		
		if (new File(getDataFolder(), "temp").exists())
			filemanager.delete(getDataFolder().getPath(), "temp");
		
		getServer().getPluginManager().registerEvents(listener, this);
		getCommand("book").setExecutor(command);
		
		if (getConfig().getBoolean("book-rules")) {
			originalRules = getServer().getPluginCommand("rules").getExecutor();
			originalQuestion = getServer().getPluginCommand("?").getExecutor();
			getServer().getPluginCommand("rules").setExecutor(rules);
			getServer().getPluginCommand("?").setExecutor(rules);
		}
		
		getLogger().info("[BookSuite] v" + version + " enabled!");
		
	}
	
	
	
	
	
	@Override
	public void onDisable() {
		if (new File(getDataFolder(), "temp").exists())
			filemanager.delete(getDataFolder().getPath(), "temp");
		try {
			if (metrics != null) {
				metrics.disable();
				metrics = null;
			}
		} catch (IOException e) {
			getLogger().warning("[BookSuite] Error disabling metrics.");
		}
		
		if (update != null)
			update.disableNotifications();
		update = null;
		
		if (perms != null)
			perms.disable();
		perms = null;
		
		alias.save();
		alias = null;
		
		command = null;
		
		//mail.disable()
		mail = null;
		
		if (getConfig().getBoolean("book-rules")) {
			getServer().getPluginCommand("rules").setExecutor(originalRules);
			getServer().getPluginCommand("?").setExecutor(originalQuestion);
		}
		rules = null;//TODO
		
		functions = null;
		
		filemanager = null;
		
		listener.disable();
		listener = null;
		
		getLogger().info("BookSuite v" + version + " disabled!");
	}
}
