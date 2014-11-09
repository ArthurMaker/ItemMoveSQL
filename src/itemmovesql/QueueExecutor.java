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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class QueueExecutor {

	public ExecutorService DBexecutor;
	Main main;
	DBUtils dbutils;
	Config config;
	ItemsGuiView igv;
	
	
	public QueueExecutor(Main main, Config config, DBUtils dbutils, ItemsGuiView igv)
	{
		DBexecutor = new ThreadPoolExecutor(config.maxthreads, config.maxthreads, 1, TimeUnit.MILLISECONDS,
			new ArrayBlockingQueue<Runnable>(config.maxthreads, true),
			new ThreadPoolExecutor.CallerRunsPolicy()
		);
		this.main = main;
		this.dbutils = dbutils;
		this.config = config;
		this.igv = igv;
	}
	
	//cmd
	public void CommandAdd(final Player player, String[] args) {
		if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
			player.sendMessage("[ItemMoveSQL] Выполняем запрос на добавление вещи в базу");
			
			final ItemStack iteminhand = player.getItemInHand();
			player.setItemInHand(null);
			
			addItemToDB(player.getName(), iteminhand);
		} else {
			player.sendMessage("[ItemMoveSQL] Нельзя добавлять пустой итем в базу");
		}
	}

	//cmd
	public void CommandView(final Player player, String[] args) {
		player.sendMessage("[ItemMoveSQL] Выполняем запрос на просмотр вещей");	
		viewItems(player.getName());
	}

	//cmd
	public void CommandGet(final Player player, String[] args) {
		if (args[1].matches("^-?\\d+$")) {
			final long getitemid = Long.valueOf(args[1]);
			player.sendMessage("[ItemMoveSQL] Выполняем запрос на получение вещи из БД");
			extractItemFromDB(player.getName(), getitemid);
		}
	}
	
	
	//operation
	private void addItemToDB(final String playername, final ItemStack iteminhand)
	{
		//serialize item
		final String item = InvConstructUtils.ItemStackToString(iteminhand);
        //create runnable
		Runnable additemtodb = new Runnable() {
			@Override
			public void run() {
				try {
					Connection conn = dbutils.getConenction();
					String statementstring = "SELECT COUNT(keyint) FROM `" + config.dbtable + "` WHERE playername = ? AND `server` = ?";
					PreparedStatement st = conn.prepareStatement(statementstring);
					st.setString(1, playername);
                                        st.setString(2, config.server);
					ResultSet result = st.executeQuery();
					result.next();
					int curiam = result.getInt(1);
					result.close();
					if (curiam < config.maxitems) {
						statementstring = "INSERT INTO `" + config.dbtable + "` (playername, item, `server`) VALUES (?,?,?)";
						st = conn.prepareStatement(statementstring);
						st.setString(1, playername);
						st.setString(2, item);
						st.setString(3, config.server);
						st.executeUpdate();
						Bukkit.getPlayerExact(playername).sendMessage("[ItemMoveSQL] Предмет успешно добавлен в базу");
						st.close();
					} else {
						st.close();
						Bukkit.getPlayerExact(playername)
								.sendMessage(
										"[ItemMoveSQL] Вы уже положили максимум вещей в базу, возвращаем вам вещь в инвентарь");
						//return item to player if needed
						giveItemToPlayer(playername, iteminhand);
					}
					conn.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		//add to executor
		DBexecutor.submit(additemtodb);
	}
	
	
	private void extractItemFromDB(final String playername, final long getitemid)
	{
		Runnable getitem = new Runnable() {
			long keyint = getitemid;
			@Override
			public void run() {
				try {
					Connection conn = dbutils.getConenction();
					String statementstring = "SELECT item, keyint FROM `" + config.dbtable + "` WHERE playername = ? AND keyint = ? AND `server` = ?";
					PreparedStatement st = conn.prepareStatement(statementstring,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
					st.setString(1, playername);
					st.setLong(2, keyint);
                                        st.setString(3, config.server);
					ResultSet result = st.executeQuery();
					if (result.next()) {
						//construct item
						ItemStack itemtogive = InvConstructUtils.StringToItemStack(result.getString(1));
						result.deleteRow();
						result.close();
						conn.close();
						//give item to player
						giveItemToPlayer(playername, itemtogive);
					} else {
						Bukkit.getPlayerExact(playername)
								.sendMessage(
										"[ItemMoveSQL] запрос на получение вещи отклонён, эта вещь вам не принадлежит");
						result.close();
						conn.close();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
		DBexecutor.submit(getitem);
	}
	
	//operation
	private void giveItemToPlayer(final String playername, final ItemStack itemtogive)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(
				main, new Runnable() {
					@Override
					public void run() {
						Bukkit.getPlayerExact(playername)
								.getInventory()
								.addItem(itemtogive);
					}
				});
	}
	
	
	//operation
	private void viewItems(final String playername) 
	{
		Runnable viewitems = new Runnable() {
			@Override
			public void run() {
				try {
					Connection conn = dbutils.getConenction();
					String statementstring = "SELECT item, keyint FROM `" + config.dbtable + "` WHERE playername = ? AND `server` = ?";
					PreparedStatement st = conn.prepareStatement(statementstring);
					st.setString(1, playername);
					st.setString(2, config.server);
					ResultSet result = st.executeQuery();
					List<ItemStack> items = new ArrayList<ItemStack>();
					while (result.next())
					{
						ItemStack showi = InvConstructUtils.StringToItemStack(result.getString(1));
						ItemMeta im = showi.getItemMeta();
						List<String> lore = new ArrayList<String>();
						if (im.hasLore()) {
							lore = im.getLore();
						}
						lore.add(ChatColor.BLUE+"==IMSQL info==");
						lore.add(ChatColor.BLUE+"/imsql get "+result.getInt(2));
						im.setLore(lore);
						showi.setItemMeta(im);
						items.add(showi);
					}
					igv.openGuiContainer(playername,items);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		DBexecutor.submit(viewitems);
	}
}
