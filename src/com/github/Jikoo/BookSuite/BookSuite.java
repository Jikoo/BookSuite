package com.github.Jikoo.BookSuite;


import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;


public class BookSuite extends JavaPlugin implements Listener{
	String version = "1.1.2";
	String neededSupplies = "";
	@Override
    public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getLogger().info("BookSuite v"+version+" enabled!");
		getCommand("makebook").setExecutor(new BookSuiteCommandExecutor(this));
    }
	@Override
    public void onDisable() {
		getLogger().info("BookSuite v"+version+" disabled!");
    }

	public boolean hasSupplies(Player p){
		Inventory i = p.getInventory();
		if (i.contains(Material.BOOK) && i.contains(Material.INK_SACK))
			return true;
		if (i.contains(Material.BOOK) && !i.contains(Material.INK_SACK))
			neededSupplies = "an ink sack";
		else if (!i.contains(Material.BOOK) && i.contains(Material.INK_SACK))
			neededSupplies = "a book";
		else if (!i.contains(Material.BOOK) && !i.contains(Material.INK_SACK))
			neededSupplies = "a book and an ink sack";
		return false;
	}


	public void copyBook(Player p, ItemStack book2copy){
		if (p.hasPermission("booksuite.copy.free")){
			if (!addBook(p, book2copy))
				p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
			return;
		}
		
		if (hasSupplies(p)){
			p.getInventory().removeItem(new ItemStack(Material.INK_SACK, 1));
			p.getInventory().removeItem(new ItemStack(Material.BOOK, 1));
			if (!addBook(p, book2copy)){
				p.getInventory().addItem(new ItemStack(Material.INK_SACK, 1));
				p.getInventory().addItem(new ItemStack(Material.BOOK, 1));
				p.sendMessage(ChatColor.DARK_RED+"Inventory full!");
			}
		}
		else{
			p.sendMessage(ChatColor.DARK_RED+"To copy a book, you need "+neededSupplies+".");
		}
	}

	public boolean addBook(Player p, ItemStack book2copy){
		if (p.getInventory().firstEmpty() == -1)return false;
		p.getInventory().addItem(book2copy.clone());
		p.updateInventory();
		p.sendMessage(ChatColor.DARK_GREEN+"Book copied!");
		return true;
	}




	@EventHandler //copy book (PrintingPress)
	public void onPlayerInteract(PlayerInteractEvent event){
		Player p = event.getPlayer();

		if (!p.hasPermission("booksuite.copy")){
			p.sendMessage(ChatColor.DARK_RED+"You do not have permission to copy books!");
			return;
		}

		ItemStack i = p.getItemInHand();

		if (i.getType().equals(Material.WRITTEN_BOOK) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType().equals(Material.WORKBENCH)){
			BookMeta b = (BookMeta) i.getItemMeta();

			if (b.getAuthor().equals(p.getName()))
				copyBook(p, i); //all players should be able to copy their own books. its silly not not allow this
			else if (p.hasPermission("booksuite.other"))
				copyBook(p, i); //this player has permission to copy anybody elses books as well
			else
				p.sendMessage(ChatColor.DARK_RED+"you do not have permission to copy others books");


			event.setCancelled(true);
		}
	}
}
