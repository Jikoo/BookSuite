package com.github.Jikoo.BookSuite;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.github.Jikoo.BookSuite.press.PrintingPress;

public class MainListener implements Listener {
	
	BookSuite plugin;
	
	private static MainListener instance;
	private MainListener(BookSuite bs) {
		this.plugin = bs;
	}
	public static MainListener getInstance(BookSuite bs) {
		if (instance == null) instance = new MainListener(bs);
		return instance;
	}
	
	
	
	/**
	 * copy book (PrintingPress) or send mail
	 * 
	 * @param event world triggered event
	 */
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

			Player p = event.getPlayer();
			ItemStack is = p.getItemInHand();
			Block clicked = event.getClickedBlock();
			Block blockUp = clicked.getRelative(BlockFace.UP);
			
			
			
			//if clicking a workbench, check to see if it is a press and act accordingly
			if (clicked.getType().equals(Material.WORKBENCH) && plugin.getConfig().getBoolean("enable-printing-presses")) {
				if (plugin.functions.isInvertedStairs(blockUp)) {
					PrintingPress press = new PrintingPress(plugin, p.getName(), blockUp);
					if (!p.hasPermission("booksuite.denynowarn.press")) {
						if (is.getType().equals(Material.MAP)) {
							if (plugin.functions.canObtainMap(p)) {
								press.operatePress();
								plugin.functions.copy(p);
								p.sendMessage(ChatColor.DARK_GREEN+"Copied successfully!");
							}
							event.setCancelled(true);
						} else if (!(is.hasItemMeta() || is.getItemMeta() != null)) {
							return;
						} else if (is.getType().equals(Material.WRITTEN_BOOK)) {
							BookMeta bm = (BookMeta) is.getItemMeta();
							if (plugin.functions.checkCopyPermission(p, bm.getAuthor()) && plugin.functions.canObtainBook(p)) {
								press.operatePress();
								plugin.functions.copy(p);
								p.sendMessage(ChatColor.DARK_GREEN+"Copied successfully!");
							}
							event.setCancelled(true);
						} else if (is.getType().equals(Material.BOOK_AND_QUILL)) {
							if (p.hasPermission("booksuite.copy.unsigned")) {
								if (plugin.functions.canObtainBook(p)) {
									press.operatePress();
									plugin.functions.copy(p);
									p.sendMessage(ChatColor.DARK_GREEN+"Copied successfully!");
								}
							} else p.sendMessage(ChatColor.DARK_RED + "You do not have permission to copy unsigned books!");
							event.setCancelled(true);
						}
					}
				} else if (event.getBlockFace().equals(BlockFace.UP) && blockUp.isEmpty() && plugin.functions.isCorrectStairType(is)) {
					if (p.hasPermission("booksuite.copy.createpress")) {
						blockUp.setTypeIdAndData(is.getTypeId(), plugin.functions.getCorrectStairOrientation(p), true);
						if (is.getAmount() == 1)
							p.setItemInHand(null);
						else is.setAmount(is.getAmount() - 1);
						event.setCancelled(true);
						p.updateInventory();
					}
				}
			} else if (is.getType().equals(Material.WRITTEN_BOOK)) {
				if (!(is.hasItemMeta() || is.getItemMeta() != null))
					return;
				if (clicked.getType().equals(Material.CAULDRON) && plugin.getConfig().getBoolean("enable-erasers")) {
					BookMeta bm = (BookMeta) is.getItemMeta();
					if (p.hasPermission("booksuite.block.erase")) {
						if (clicked.getData() < 1 && !p.getGameMode().equals(GameMode.CREATIVE) && !p.hasPermission("booksuite.block.erase.free"))
							p.sendMessage(ChatColor.DARK_RED + "You'll need some water to unsign this book.");
						else if (bm.getAuthor().equalsIgnoreCase(p.getDisplayName())) {
							plugin.functions.unsign(p);
							if (!p.hasPermission("booksuite.block.erase.free") && !p.getGameMode().equals(GameMode.CREATIVE))
								clicked.setData((byte) (clicked.getData() - 1));
						} else if (p.hasPermission("booksuite.block.erase.other")) {
							plugin.functions.unsign(p);
							if(!p.hasPermission("booksuite.block.erase.free") && !p.getGameMode().equals(GameMode.CREATIVE))
								clicked.setData((byte) (clicked.getData() - 1));
						} else p.sendMessage(ChatColor.DARK_RED + "You can only unsign your own books.");
						event.setCancelled(true);
					} else if (!p.hasPermission("booksuite.denynowarn.erase")) {
						p.sendMessage(ChatColor.DARK_RED + "You do not have permission to use erasers.");
						event.setCancelled(true);
					}
				}
			}
			
			
			
			//this is for checking mail
			if (clicked.getType().equals(Material.CHEST))
				if (blockUp.getType().equals(Material.SIGN)) {
					Sign sign = (Sign) blockUp;
					if (sign.getLine(0).equals(ChatColor.DARK_RED+"No sign line can contain this string.")){//rudimentary example
						p.openInventory(plugin.mail.getMailBoxInv(p, plugin.getDataFolder().getPath()));
						event.setCancelled(true);
					}
				} 
			
		}
		
		
		// this is for taking a "package/envelope" that contains a "gift" and opening it into your inventory.
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR)) {
			Player p = event.getPlayer();
			if (p.getItemInHand().getType().equals(Material.WRITTEN_BOOK)) {
				if (!(p.getItemInHand().hasItemMeta()||p.getItemInHand().getItemMeta() != null))
					return;
				BookMeta bm = (BookMeta) p.getItemInHand().getItemMeta();
				if (bm.getTitle().contains("Package: ")) {
					if (plugin.mail.loadMail(p, bm, plugin.getDataFolder().getPath()))
						event.setCancelled(true);
				}
				else if (p.hasPermission("booksuite.mail.send") && bm.getTitle().equalsIgnoreCase("package"))
					if (plugin.mail.sendMail(p, bm, plugin.getDataFolder().getPath()))
						event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onInventoryClose (InventoryCloseEvent event) {
		if (event.getInventory().getTitle().contains("'s MailBox")) {
			plugin.mail.WriteMailContents(event.getInventory());
		}
	}
	
	public void disable() {
		HandlerList.unregisterAll(this);
	}
}
