package promcgames.server.servers.hub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import promcgames.customevents.player.NewPlayerJoiningEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.CommunityLevelHandler;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

public class WelcomeHandler implements Listener {
	private int counter = 10;
	private List<String> saidWelcome = null;
	
	public WelcomeHandler() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onNewPlayerJoiningEvent(NewPlayerJoiningEvent event) {
		Player player = event.getPlayer();
		EffectUtil.launchFirework(new Location(player.getWorld(), 16.5, 26, -2.5));
		EffectUtil.launchFirework(new Location(player.getWorld(), 16.5, 26, 3.5));
		counter = 10;
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		--counter;
		if(counter <= 0 && saidWelcome != null) {
			saidWelcome.clear();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(!event.isCancelled() && counter > 0 && event.getMessage().toLowerCase().startsWith("welcome") && (saidWelcome == null || !saidWelcome.contains(event.getPlayer().getName()))) {
			if(saidWelcome == null) {
				saidWelcome = new ArrayList<String>();
			}
			saidWelcome.add(event.getPlayer().getName());
			CommunityLevelHandler.addCommunityLevel(event.getPlayer(), 3);
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!event.getRecipients().contains(player)) {
					event.getRecipients().add(player);
				}
			}
		}
	}
}
