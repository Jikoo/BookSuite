package com.github.Jikoo.BookSuite;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class BookSuite extends JavaPlugin implements Listener{
	@Override
    public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("BookSuite v1.0.0 enabled!");
    }
	@Override
    public void onDisable() {
		getLogger().info("BookSuite v1.0.0 disabled!");
    }
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if(event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
			if (event.getClickedBlock().getType().equals(Material.WORKBENCH) && event.getPlayer().getItemInHand().getType().equals(Material.WRITTEN_BOOK)){
				if (!event.getPlayer().hasPermission("booksuite.copy"))
					event.getPlayer().sendMessage(ChatColor.DARK_RED+"You do not have permission to copy books!");
				else if (event.getPlayer().getInventory().firstEmpty() == -1)
					event.getPlayer().sendMessage(ChatColor.DARK_RED+"Inventory full!");
				else {
					event.getPlayer().sendMessage(ChatColor.DARK_GREEN+"Book copied!");
					event.getPlayer().getInventory().addItem(event.getPlayer().getItemInHand());
					event.getPlayer().updateInventory();
				}
			event.setCancelled(true);
			}
		}
	}
}
