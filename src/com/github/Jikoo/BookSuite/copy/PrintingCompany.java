package com.github.Jikoo.BookSuite.copy;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.github.Jikoo.BookSuite.Functions;
import com.github.Jikoo.BookSuite.module.BookSuiteModule;

public class PrintingCompany implements BookSuiteModule {

	private Map<Block, PrintingPress> presses = new HashMap<Block, PrintingPress>();
	private boolean enabled = true;

	/**
	 * adds a printing press to the set, that can be accessed either by the base
	 * block or head block of the press
	 * 
	 * @param base
	 *            the base block (crafting table)
	 */
	public void makePress(Block base) {
		PrintingPress p = new PrintingPress(base);
		this.presses.put(base, p);
		this.presses.put(base.getRelative(BlockFace.UP), p);
	}

	/**
	 * 
	 * @param eventBlock
	 *            the block on which an event was fired
	 * @return the printing press in existence on the location of the block
	 */
	public PrintingPress getPrintingPress(Block eventBlock) {
		PrintingPress p = presses.get(eventBlock);
		if (p != null) {
			return p;
		}
		if (eventBlock.getType().equals(Material.WORKBENCH)) {
			makePress(eventBlock);
		} else if (Functions.getInstance().isInvertedStairs(eventBlock)) {
			makePress(eventBlock.getRelative(BlockFace.DOWN));
		}
		return presses.get(eventBlock);
	}

	@Override
	public boolean isTriggeredByEvent(Event e) {
		if (e instanceof PlayerInteractEvent) {
			PlayerInteractEvent pie = (PlayerInteractEvent) e;
			if (Functions.getInstance().isPrintingPress(pie.getClickedBlock())) {
				PrintingPress p = getPrintingPress(pie.getClickedBlock());
				pie.setCancelled(this.manipulate(pie.getPlayer(), p));
			}
		}
		return false;
	}

	private boolean manipulate(Player p, PrintingPress press) {
		if (!p.hasPermission("booksuite.denynowarn.press")) {
			ItemStack is = p.getItemInHand();
			if (is.getType().equals(Material.MAP)) {
				if (Functions.getInstance().canObtainMap(p)) {
					press.operatePress();
					Functions.getInstance().copy(p);
					p.sendMessage(ChatColor.DARK_GREEN + "Copied successfully!");
				}
				return true;
			} else if (is.getType().equals(Material.WRITTEN_BOOK)) {
				BookMeta bm = (BookMeta) is.getItemMeta();

				if (Functions.getInstance().checkCopyPermission(p,
						bm.getAuthor())
						&& Functions.getInstance().canObtainBook(p)) {
					press.operatePress();
					Functions.getInstance().copy(p);
					p.sendMessage(ChatColor.DARK_GREEN + "Copied successfully!");
				}
				return true;
			} else if (is.getType().equals(Material.BOOK_AND_QUILL)) {
				if (p.hasPermission("booksuite.copy.unsigned")) {
					if (Functions.getInstance().canObtainBook(p)) {
						press.operatePress();
						Functions.getInstance().copy(p);
						p.sendMessage(ChatColor.DARK_GREEN
								+ "Copied successfully!");
					}
				} else {
					p.sendMessage(ChatColor.DARK_RED
							+ "You do not have permission to copy unsigned books!");
				}
				return true;
			} else if (!(is.hasItemMeta() || is.getItemMeta() != null)) {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean isTriggeringCommand(Command c, String[] args,
			CommandSender sender, String label) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public int disable() {
		this.enabled = false;
		return 0;
	}

}