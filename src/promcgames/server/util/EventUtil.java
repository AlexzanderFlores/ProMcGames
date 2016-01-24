package promcgames.server.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.player.MessageHandler;
import promcgames.server.CommandBase;

public class EventUtil {
	private static List<String> listeners = null;
	
	public static void register(Listener listener) {
		if(ProMcGames.getPlugin() == Plugins.CLAN_BATTLES) {
			if(listeners == null) {
				listeners = new ArrayList<String>();
				new CommandBase("test") {
					@Override
					public boolean execute(CommandSender sender, String [] arguments) {
						for(String listener : listeners) {
							MessageHandler.sendMessage(sender, listener.toString());
						}
						return true;
					}
				};
			}
			String name = listener.toString().split("@")[0];
			if(!listeners.contains(name)) {
				listeners.add(name);
				Bukkit.getPluginManager().registerEvents(listener, ProMcGames.getInstance());
			}
		} else {
			Bukkit.getPluginManager().registerEvents(listener, ProMcGames.getInstance());
		}
	}
}
