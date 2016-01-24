package promcgames.staff;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.ProMcGames;
import promcgames.server.ProPlugin;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.StringUtil;

public class StaffChat implements Listener {
	public StaffChat() {
		new CommandBase("s", 1, -1) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(sender instanceof Player) {
					Player player = (Player) sender;
					String message = "&e[" + ProMcGames.getServerName().toLowerCase() + "&e] " + AccountHandler.getPrefix(player, true, true) + ": ";
					for(String argument : arguments) {
						message += argument + " ";
					}
					ProPlugin.dispatchCommandToAll("s " + message);
				} else {
					String message = "";
					for(String argument : arguments) {
						message += argument + " ";
					}
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(Ranks.isStaff(player)) {
							MessageHandler.sendMessage(player, "&bStaff: " + StringUtil.color(message.substring(0, message.length() - 1)));
						}
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.HELPER);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(ProMcGames.getPlugin() == Plugins.UHC) {
			HandlerList.unregisterAll(this);
		} else {
			if(Ranks.isStaff(event.getPlayer())) {
				String name = AccountHandler.getPrefix(event.getPlayer());
				for(Player player : Bukkit.getOnlinePlayers()) {
					if(Ranks.isStaff(player)) {
						MessageHandler.sendMessage(player, "&bStaff: " + name + " has joined this server");
					}
				}
			}
		}
	}
}
