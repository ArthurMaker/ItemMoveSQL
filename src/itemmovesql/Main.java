package itemmovesql;

import java.util.concurrent.TimeUnit;

import org.bukkit.plugin.java.JavaPlugin;
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


public class Main extends JavaPlugin {

	private DBUtils dbutils;
	private Config config;
	private Commands commands;
	private QueueExecutor executor;
	ItemsGuiView guiview;

	public void onEnable() {
		config = new Config();
		config.load();
		dbutils = new DBUtils(this, config);
		dbutils.createNeededTable();
		guiview = new ItemsGuiView(this);
		getServer().getPluginManager().registerEvents(guiview, this);
		executor = new QueueExecutor(this, config, dbutils,guiview);
		commands = new Commands(executor);
		getCommand("imsql").setExecutor(commands);
		
	}

	public void onDisable() {
		executor.DBexecutor.shutdown();
		try {
			executor.DBexecutor.awaitTermination(1, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		config.save();
	}


}
