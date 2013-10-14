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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
					Statement st;
					Connection conn = dbutils.getConenction();
					st = conn.createStatement();
					ResultSet result = st
							.executeQuery("SELECT COUNT(keyint) FROM itemstorage WHERE playername = '"
									+ playername + "'");
					result.next();
					int curiam = result.getInt(1);
					result.close();
					if (curiam < config.maxitems) {
						st.executeUpdate("INSERT INTO itemstorage (playername, item) VALUES ('"
								+ playername
								+ "', '"
								+ item
								+ "')"
								);
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
					Statement st;
					Connection conn = dbutils.getConenction();
					st = conn.createStatement(
							ResultSet.TYPE_SCROLL_SENSITIVE,
							ResultSet.CONCUR_UPDATABLE);
					ResultSet result = st
							.executeQuery("SELECT item, keyint FROM itemstorage WHERE playername = '"
									+ playername
									+ "' AND keyint = "
									+ keyint
									);
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
					Statement st;
					Connection conn = dbutils.getConenction();
					st = conn.createStatement();
					ResultSet result = st
							.executeQuery("SELECT item, keyint FROM itemstorage WHERE playername = '"
									+ playername
									+ "'"
									);
					igv.openGuiContainer(playername,result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		DBexecutor.submit(viewitems);
	}
}
