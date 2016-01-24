package promcgames.server;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.timed.FiveSecondTaskEvent;
import promcgames.customevents.timed.OneMinuteTaskEvent;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class CommandDispatcher implements Listener {
	private static List<String> hubs = null;
	
	public CommandDispatcher() {
		hubs = new ArrayList<String>();
		loadHubs();
		new CommandBase("dispatchCommand", 1, -1, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				String server = arguments[0];
				String command = "";
				for(int a = 1; a < arguments.length; ++a) {
					command += arguments[a] + " ";
				}
				dispatch(server, command.substring(0, command.length() - 1));
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		new CommandBase("globalHubUpdate", 0, 1, false) {
			@Override
			public boolean execute(CommandSender sender, String [] arguments) {
				if(arguments.length == 1 && arguments[0].equalsIgnoreCase("stop")) {
					for(String hub : hubs) {
						DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.insert("'" + hub + "', 'restartServer stop'");
					}
				} else {
					dispatchToHubs("say &bGlobal hub restart initialized");
					for(String hub : hubs) {
						DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.insert("'" + hub + "', 'restartServer " + hub.replace("HUB", "") + "'");
					}
				}
				return true;
			}
		}.setRequiredRank(Ranks.OWNER);
		EventUtil.register(this);
	}
	
	private void loadHubs() {
		hubs.clear();
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				int size = DB.NETWORK_SERVER_STATUS.getSize("game_name", "HUB");
				for(int a = 1; a <= size; ++a) {
					hubs.add("HUB" + a);
				}
			}
		});
	}
	
	public static void dispatchToHubs(String command) {
		for(String hub : hubs) {
			DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.insert("'" + hub + "', '" + command + "'");
		}
	}
	
	public static void dispatch(String server, String command) {
		DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.insert("'" + server + "', '" + command + "'");
	}
	
	@EventHandler
	public void onFiveSecondTask(FiveSecondTaskEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				List<String> commands = DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.getAllStrings("command", "server", ProMcGames.getServerName());
				if(commands != null && !commands.isEmpty()) {
					for(String command : commands) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
					}
					DB.NETWORK_BUKKIT_COMMAND_DISPATCHER.delete("server", ProMcGames.getServerName());
				}
			}
		});
	}
	
	@EventHandler
	public void onOneMinuteTask(OneMinuteTaskEvent event) {
		loadHubs();
	}
}
