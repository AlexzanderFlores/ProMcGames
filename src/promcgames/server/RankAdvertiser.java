package promcgames.server;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.timed.OneMinuteTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.util.EventUtil;

public class RankAdvertiser implements Listener {
	private static List<String> messages = null;
	private static int counter = 0;
	
	public RankAdvertiser() {
		addMessage("Ranks give you Emerald multipliers");
		addMessage("Ranks aren't displayed rank ads");
		addMessage("Ranks give you more map votes");
		addMessage("Ranks give you colored chat");
		addMessage("Ranks give you many cool hub perks");
		addMessage("Ranks bypass the 3 second chat delay");
		EventUtil.register(this);
	}
	
	public static void addMessage(String message) {
		if(messages == null) {
			messages = new ArrayList<String>();
		}
		messages.add("&e" + message + " &b/buy");
	}
	
	@EventHandler
	public void onOneMinuteTask(OneMinuteTaskEvent event) {
		if(++counter >= messages.size()) {
			counter = 0;
		}
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(!Ranks.PRO.hasRank(player, true)) {
				MessageHandler.sendMessage(player, "&b[Ad] &e" + messages.get(counter));
			}
		}
	}
}
