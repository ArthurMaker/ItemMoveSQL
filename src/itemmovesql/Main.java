package itemmovesql;

import java.util.concurrent.TimeUnit;

import org.bukkit.plugin.java.JavaPlugin;

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
