package itemmovesql;

import java.util.concurrent.TimeUnit;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

	private DBUtils dbutils;
	private ItemMoveSQLConfig config;
	private Commands commands;
	private QueueExecutor executor;

	public void onEnable() {
		config = new ItemMoveSQLConfig();
		config.load();
		dbutils = new DBUtils(this, config);
		dbutils.createNeededTable();
		executor = new QueueExecutor(this, config, dbutils);
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
