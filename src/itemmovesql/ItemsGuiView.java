package itemmovesql;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemsGuiView implements Listener {
	
	private Main main;
	
	private HashMap<String, InventoryView> playerGuiInv = new HashMap<String, InventoryView>();
	
	public ItemsGuiView(Main main)
	{
		this.main = main;
	}
	
	public void openGuiContainer(final String player, final ResultSet rs)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable()
		{
			public void run()
			{
				try {
				String pl = player;
				Inventory guidb = Bukkit.getServer().createInventory(null, 27, ChatColor.BLUE+"Your items in database");
				//add items to virtual inventory
				while (rs.next())
				{
					ItemStack showi = InvConstructUtils.ResultSetToItemStack(rs);
					ItemMeta im = showi.getItemMeta();
					List<String> lore = null;
					if (im.hasLore()) {
					lore = im.getLore();
					} else {
						lore = new ArrayList<String>();
					}
					lore.add(ChatColor.BLUE+"==IMSQL info==");
					lore.add(ChatColor.BLUE+"/imsql get "+rs.getInt(8));
					im.setLore(lore);
					showi.setItemMeta(im);
					guidb.addItem(showi);
				}
				//openinventory
				InventoryView iv = Bukkit.getPlayerExact(pl).openInventory(guidb);
				playerGuiInv.put(pl, iv);
				} catch (Exception e) {
				e.printStackTrace();
				}
			}
		});
	}
	
	
	//do not allow to get items from inventory
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onPlayerClickedGuiView(InventoryClickEvent e)
	{
		String pl = e.getWhoClicked().getName();
		if (playerGuiInv.containsKey(pl))
		{
			if (playerGuiInv.get(pl).getTopInventory().equals(e.getInventory()))
			{
				e.setCancelled(true);
			}
		}
	}
	//do not allow to get items from inventory
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onPlayerDraggedGuiView(InventoryDragEvent e)
	{
		String pl = e.getWhoClicked().getName();
		if (playerGuiInv.containsKey(pl))
		{
			if (playerGuiInv.get(pl).getTopInventory().equals(e.getInventory()))
			{
				e.setCancelled(true);
			}
		}
	}
	
	
	//remove inventory from list on inventory close
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onPlayedClosedInv(InventoryCloseEvent e)
	{
		playerGuiInv.remove(e.getPlayer().getName());
	}
	//remove inventory from list on player quit
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onPlayedQuit(PlayerQuitEvent e)
	{
		playerGuiInv.remove(e.getPlayer().getName());
	}
	//remove inventory from list on player kicked
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onPlayedKicked(PlayerKickEvent e)
	{
		playerGuiInv.remove(e.getPlayer().getName());
	}
	
	
}
