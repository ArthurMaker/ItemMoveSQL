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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands  implements CommandExecutor {

	private QueueExecutor executor;
	
	public Commands(QueueExecutor executor) {
		this.executor = executor;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String cl, String[] args) 
	{
		String cname = command.getName();
		final Player player;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			sender.sendMessage("Данные комманды недоступны в консоли");
			return true;
		}
		if (cname.equalsIgnoreCase("imsql")) {
			if (args.length == 1 && args[0].equalsIgnoreCase("add")) {
				executor.CommandAdd(player, args);
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("view")) {
				executor.CommandView(player, args);
				return true;
			} else if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
				executor.CommandGet(player, args);
				return true;
			} else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
				viewHelp(player);
			}

		}
		return false;
	}

	private void viewHelp(Player player)
	{
		player.sendMessage("[ItemMoveSQL] /imsql add - положить предмет в БД");
		player.sendMessage("[ItemMoveSQL] /imsql get {номер} - получить вещь из БД");
		player.sendMessage("[ItemMoveSQL] /imsql view - посмотреть свои вещи в БД");
	}

}
