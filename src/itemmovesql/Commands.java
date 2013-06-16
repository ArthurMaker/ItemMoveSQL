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
	public boolean onCommand(CommandSender sender, Command command, String cl,
			String[] args) {
		String cname = command.getName();
		final Player player;
		if (sender instanceof Player) {
			player = (Player) sender;
		} else {
			return false;
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
			}

		}
		return false;
	}


}
