package promcgames.server;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProMcGames;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.timed.OneMinuteTaskEvent;
import promcgames.gameapi.games.uhc.HostedEvent;
import promcgames.server.util.EventUtil;
import promcgames.server.util.StringUtil;
import promcgames.server.util.UnicodeUtil;

public class AutoBroadcasts implements Listener {
	private static List<String> messages = null;
	private int counter = 0;
	
	public AutoBroadcasts() {
		if(ProMcGames.getPlugin() == Plugins.UHC && HostedEvent.isEvent()) {
			return;
		}
		addAlert("Learn about our rules &b/rules");
		addAlert("Join our forums &b/forums");
		addAlert("Talk to other players on Teamspeak &b/ts");
		EventUtil.register(this);
	}
	
	public static void addAlert(String text) {
		if(messages == null) {
			messages = new ArrayList<String>();
		}
		messages.add(text);
	}
	
	@EventHandler
	public void onOneMinuteTask(OneMinuteTaskEvent event) {
		if(Bukkit.getOnlinePlayers().size() > 0) {
			Bukkit.broadcastMessage(StringUtil.color("&3[&e" + UnicodeUtil.getUnicode(2748) + "&3] &a" + messages.get(counter++)));
			if(counter >= messages.size()) {
				counter = 0;
			}
		}
	}
}
