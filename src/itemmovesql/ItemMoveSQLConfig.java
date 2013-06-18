package itemmovesql;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ItemMoveSQLConfig {

	protected String address = "jdbc:mysql://localhost/";
	protected String dbname = "itemmovesqldb";
	protected String login = "root";
	protected String pass = "password";
	protected int maxitems = 5;
	protected boolean checkdb = true;
	protected int maxthreads = 8;

	public void load() {
		FileConfiguration config = YamlConfiguration
				.loadConfiguration(new File("plugins/ItemMoveSQL/config.yml"));
		address = config.getString("mysql.address", address);
		dbname = config.getString("mysql.dbname", dbname);
		login = config.getString("mysql.login", login);
		pass = config.getString("mysql.password", pass);
		checkdb = config.getBoolean("mysql.checkdb", checkdb);
		maxitems = config.getInt("items.max", maxitems);
		maxthreads = config.getInt("mysql.maxparallelrequests", maxthreads);

		save();
	}
	
	public void save()
	{
		FileConfiguration config = new YamlConfiguration();
		config.set("mysql.address", address);
		config.set("mysql.dbname", dbname);
		config.set("mysql.login", login);
		config.set("mysql.password", pass);
		config.set("mysql.checkdb", checkdb);
		config.set("items.max", maxitems);
		config.set("mysql.maxparallelrequests", maxthreads);

		try {
			config.save(new File("plugins/ItemMoveSQL/config.yml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
