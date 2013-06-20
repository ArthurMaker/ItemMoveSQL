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
		//item id
		final int itemid = iteminhand.getTypeId();
		//item subid
		final int subdurabid = iteminhand.getDurability();
		//amount
		final int amount = iteminhand.getAmount();
		//enchants
		final String enchants;
		if (iteminhand.getItemMeta().hasEnchants()) {
			enchants = InvConstructUtils.EnchantmentsToString(iteminhand);
		} 
		else {
			enchants = "none";
		}
		//lore
		final String lore;
		if (iteminhand.getItemMeta().hasLore()) {
			lore = InvConstructUtils.LoreToString(iteminhand);
		} 
		else {
			lore = "none";
		}
		//displayname
		String dispname;
        final String displayname;
		if (iteminhand.getItemMeta().hasDisplayName()) {
			dispname =  iteminhand.getItemMeta().getDisplayName();
	        	 dispname = dispname.replaceAll("\\\\", "\\\\\\\\");
	        	 dispname = dispname.replaceAll("\\n","\\\\n");
	        	 dispname = dispname.replaceAll("\\r", "\\\\r");
	        	 dispname = dispname.replaceAll("\\t", "\\\\t");
	        	 dispname = dispname.replaceAll("\\00", "\\\\0");
	        	 dispname = dispname.replaceAll("'", "\\\\'");
	        	 dispname = dispname.replaceAll("\\\"", "\\\\\"");
		}
		else {
			dispname = "none";
		}
        displayname = dispname;
		
		
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
						st.executeUpdate("INSERT INTO itemstorage (playername, itemid, itemsubid, amount, enchants, lore, displayname) VALUES ('"
								+ playername
								+ "', '"
								+ itemid
								+ "', '"
								+ subdurabid
								+ "', '"
								+ amount
								+ "', '"
								+ enchants
								+ "', '"
								+ lore
								+ "', '"
								+ displayname
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
							.executeQuery("SELECT keyint ,itemid, itemsubid, amount, enchants, lore, displayname FROM itemstorage WHERE playername = '"
									+ playername
									+ "' AND keyint = "
									+ keyint
									);
					if (result.next()) {
						//construct item
						ItemStack itemtogive = InvConstructUtils.ResultSetToItemStack(result);
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
							.executeQuery("SELECT playername, itemid, itemsubid, amount, enchants, lore, displayname, keyint FROM itemstorage WHERE playername = '"
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
