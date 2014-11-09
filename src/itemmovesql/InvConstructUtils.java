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

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class InvConstructUtils {

	public static ItemStack StringToItemStack(String string)
	{
		ItemStack itemtogive = new ItemStack(Material.COBBLESTONE);
		try {
			FileConfiguration config = new YamlConfiguration();
			config.loadFromString(string);
			itemtogive = config.getItemStack("item");
		} catch (InvalidConfigurationException e) {}
		return itemtogive;
	}
	
	public static String ItemStackToString(ItemStack is)
	{
		FileConfiguration config = new YamlConfiguration();
		config.set("item", is);
		return config.saveToString();
	}

}
