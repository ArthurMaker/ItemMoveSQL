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

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {

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
