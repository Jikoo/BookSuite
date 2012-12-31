package com.github.Jikoo.BookSuite;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;


public class BookSuite extends JavaPlugin implements Listener{
	String version = "1.1.0";
	@Override
    public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("BookSuite v"+version+" enabled!");
    }
	@Override
    public void onDisable() {
		getLogger().info("BookSuite v"+version+" disabled!");
    }
	@SuppressWarnings("deprecation")
	@EventHandler //copy book (PrintingPress)
	public void onPlayerInteract(PlayerInteractEvent event){
		if (event.getPlayer().hasPermission("booksuite.denycopy"))
			return;
		if (event.getPlayer().getItemInHand().getType().equals(Material.WRITTEN_BOOK) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType().equals(Material.WORKBENCH)){
			Player p = event.getPlayer();
			ItemStack i = p.getItemInHand();
			BookMeta b = (BookMeta) i.getItemMeta();
			if (!p.hasPermission("booksuite.copy.self"))
				p.sendMessage(ChatColor.DARK_RED+"You do not have permission to copy books!");
			else if (!p.hasPermission("booksuite.copy.other") && !b.getAuthor().equals(p.getName()))
				p.sendMessage(ChatColor.DARK_RED+"You do not have permission to copy others' books!");
			else if (p.getInventory().firstEmpty() == -1)
				p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
			else {
				p.sendMessage(ChatColor.DARK_GREEN+"Book copied!");
				p.getInventory().addItem(i);
				p.updateInventory();
			}
			event.setCancelled(true);
		}
	}
}
