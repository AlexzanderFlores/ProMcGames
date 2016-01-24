package promcgames.server.servers.worker;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

import promcgames.customevents.timed.FiveMinuteTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.CommandBase;
import promcgames.server.CommandDispatcher;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.servers.hub.RankTransferHandler;
import promcgames.staff.Punishment;

public class Worker extends ProPlugin {
	public Worker() {
		super("Worker");
		new RankTransferHandler();
		new CommandBase("purchase", -1, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				String packageName = arguments[1];
				if(arguments.length > 2) {
					for(int a = 2; a < arguments.length; ++a) {
						packageName += arguments[a];
					}
				}
				if(packageName.toLowerCase().contains("gift")) {
					return true;
				}
				Bukkit.getLogger().info(packageName);
				DB.NETWORK_RECENT_PURCHASES.insert("'" + name + ": " + packageName + "'");
				CommandDispatcher.dispatchToHubs("purchase");
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("unmutepass", 1, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String name = arguments[0];
				UUID uuid = AccountHandler.getUUID(name);
				String reason = Punishment.ChatViolations.SERVER_ADVERTISEMENT.toString();
				if(uuid == null) {
					MessageHandler.sendMessage(sender, "&c" + name + " has never logged in before");
				} else if(DB.STAFF_MUTES.isKeySet(new String [] {"uuid", "reason"}, new String [] {uuid.toString(), reason})) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "unmute " + name + " Purchased unmute pass");
				} else {
					MessageHandler.sendMessage(sender, "&c" + name + " is not muted for " + reason);
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
	}
	
	@EventHandler
	public void onFiveMinuteTask(FiveMinuteTaskEvent event) {
		ProPlugin.restartServer();
	}
}
