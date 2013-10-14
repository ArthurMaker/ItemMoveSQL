/**
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
*
*/

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
						ItemStack showi = InvConstructUtils.StringToItemStack(rs.getString(1));
						ItemMeta im = showi.getItemMeta();
						List<String> lore = new ArrayList<String>();
						if (im.hasLore()) {
							lore = im.getLore();
						}
						lore.add(ChatColor.BLUE+"==IMSQL info==");
						lore.add(ChatColor.BLUE+"/imsql get "+rs.getInt(2));
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
