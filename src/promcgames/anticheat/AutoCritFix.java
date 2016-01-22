package promcgames.anticheat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import promcgames.customevents.timed.TwoSecondTaskEvent;
import promcgames.server.util.EventUtil;

public class AutoCritFix extends AntiGamingChair implements Listener {
	private Map<String, Integer> counters = null;
	
	public AutoCritFix() {
		super("Auto Crit");
		counters = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			double y = event.getTo().getY() - event.getTo().getBlockY();
			if(!player.getAllowFlight() && (y > 0.12999999 && y < 0.13) || (y > 0.25999999 && y < 0.26)) {
				int counter = 1;
				if(counters.containsKey(player.getName())) {
					counter = counters.get(player.getName());
				}
				if(++counter >= 5) {
					ban(player);
				} else {
					counters.put(player.getName(), counter);
				}
			}
		}
	}
	
	@EventHandler
	public void onTwoSecondTask(TwoSecondTaskEvent event) {
		if(isEnabled()) {
			counters.clear();
		}
	}
}
