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

import java.sql.*;

import java.sql.Connection;
import java.util.logging.Logger;

import org.bukkit.Bukkit;

public class DBUtils {

	private Config config;

	DBUtils(Main main, Config config) {
		this.config = config;
	}

	private Logger log = Bukkit.getLogger();

	public Connection getConenction() {
		return InitConnection();
	}

	public void createNeededTable() {
		Connection connection = null;
		try {
			if (config.checkdb) {
				connection = DriverManager.getConnection(config.address,
						config.login, config.pass);
				log.info("[ItemMoveMSQL] Connected to mysql server, creating database if not exists");
				Statement st = connection.createStatement();
				st.executeUpdate("CREATE DATABASE IF NOT EXISTS "
						+ config.dbname);
				st.close();
				connection.close();
			}
			connection = DriverManager.getConnection(config.address
					+ config.dbname, config.login, config.pass);
			Statement st = connection.createStatement();
			st.executeUpdate("CREATE TABLE IF NOT EXISTS itemstorage"
					+ "("
					+ "keyint int unsigned not null auto_increment primary key,"
					+ "playername varchar(255),"
					+ "item text"
					+ ");"
					);
			st.close();
			log.info("[ItemMoveMSQL] Connected to mysql server and database");
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Connection InitConnection() {
		try {
			Connection connection = null;

			connection = DriverManager.getConnection(config.address
					+ config.dbname, config.login, config.pass);

			return connection;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}

}
