package com.github.Jikoo.BookSuite;

import java.io.File;
import java.io.IOException;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.github.Jikoo.BookSuite.metrics.Metrics;
import com.github.Jikoo.BookSuite.permissions.PermissionsListener;
import com.github.Jikoo.BookSuite.rules.Rules;
import com.github.Jikoo.BookSuite.update.UpdateCheck;
import com.github.Jikoo.BookSuite.update.UpdateConfig;


public class BookSuite extends JavaPlugin implements Listener {
	String version = "3.1.2";
	public int currentFile = 11;
	public boolean hasUpdate;
	public String updateString;

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

	private static BookSuite instance;

	@Override
	public void onEnable() {
		getLogger().info("Initializing.");
		
		instance = this;
		
		saveDefaultConfig();
		
		if (new UpdateConfig(this).update())
			getLogger().warning("Your configuration has been changed, please check it!");
		
		mail = MailExecutor.getInstance();
		functions = Functions.getInstance();
		filemanager = FileManager.getInstance();
		
		alias = Alias.getInstance();
		alias.load();
		
		if (getConfig().getBoolean("update-check") || getConfig().getBoolean("allow-update-command"))
			update = new UpdateCheck(this);
		
		try {
			if (getConfig().getBoolean("use-inbuilt-permissions")) {
				getLogger().info("Enabling inbuilt permissions.");
				perms = new PermissionsListener(this);
				perms.enable();
			}
			
			
			if (getConfig().getBoolean("enable-metrics")) {
				getLogger().info("Enabling metrics.");
				try {
					metrics = new Metrics(this);
					metrics.start();
				} catch (IOException e) {
					getLogger().warning("Error enabling metrics: " + e);
					e.printStackTrace();
					getLogger().warning("End error report.");
					if (metrics != null) {
						metrics.disable();
						metrics = null;
					}
				}
			}
			
			
			if(getConfig().getBoolean("update-check")) {
				if(getConfig().getBoolean("login-update-check")) {
					getLogger().info("Enabling login update check.");
					update.enableNotifications();
				}
				
				getLogger().info("Starting update check...");
				
				update.asyncUpdateCheck(null, false);
			}
			
			
			if (getConfig().getBoolean("book-rules")) {
				rules = new Rules();
				rules.enable();
			}
			
			
		} catch (Exception e) {
			getLogger().warning("Error loading configuration: " + e);
			e.printStackTrace();
			getLogger().warning("End error report.");
		}
		
		if (new File(getDataFolder(), "temp").exists())
			filemanager.delete(getDataFolder().getPath(), "temp");
		
		listener = MainListener.getInstance();
		getServer().getPluginManager().registerEvents(listener, this);
		command = CommandHandler.getInstance();
		getCommand("book").setExecutor(command);
		
		getLogger().info("v" + version + " enabled!");
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
			getLogger().warning("Error disabling metrics.");
		}
		
		if (update != null) {
			update.disableNotifications();
			update = null;
		}
		
		if (perms != null)
			perms.disable();
		perms = null;
		
		alias.save();
		alias = null;
		
		command = null;
		
		if (mail != null) {
			//TODO mail.disable()
			mail = null;
		}
		
		if (rules != null) {
			rules.disable();
			rules = null;
		}
		
		functions = null;
		
		filemanager = null;
		
		listener.disable();
		listener = null;
		
		instance = null;
		
		getLogger().info("BookSuite v" + version + " disabled!");
	}





	public static BookSuite getInstance() {
		return instance;
	}
}
