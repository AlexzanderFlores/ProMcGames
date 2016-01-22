package promcgames.player;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.OneTickTaskEvent;
import promcgames.server.util.EventUtil;

public class AliveTracker implements Listener {
	private static Map<String, Integer> aliveCounter = null;
	
	public AliveTracker() {
		aliveCounter = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	public static int getAliveTicks(Player player) {
		return aliveCounter == null ? -1 : aliveCounter.containsKey(player.getName()) ? aliveCounter.get(player.getName()) : -1;
	}
	
	public static int getAliveSeconds(Player player) {
		return getAliveTicks(player) / 20;
	}
	
	@EventHandler
	public void onOneTickTask(OneTickTaskEvent event) {
		for(String name : aliveCounter.keySet()) {
			aliveCounter.put(name, aliveCounter.get(name) + 1);
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		aliveCounter.put(event.getPlayer().getName(), 0);
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		aliveCounter.remove(event.getPlayer().getName());
	}
}
