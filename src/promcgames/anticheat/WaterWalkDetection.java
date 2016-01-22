package promcgames.anticheat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.player.WaterSplashEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.server.util.EventUtil;

public class WaterWalkDetection extends AntiGamingChair implements Listener {
	private Map<String, Integer> counters = null;
	
	public WaterWalkDetection() {
		super("Water Walking");
		counters = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(isEnabled()) {
			counters.clear();
		}
	}
	
	@EventHandler
	public void onWaterSplash(WaterSplashEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			int counter = 0;
			if(counters.containsKey(player.getName())) {
				counter = counters.get(player.getName());
			}
			if(++counter >= 5) {
				ban(player);
			}
			counters.put(player.getName(), counter);
		}
	}
}
