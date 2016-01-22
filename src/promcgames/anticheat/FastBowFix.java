package promcgames.anticheat;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.PerformanceHandler;
import promcgames.server.util.EventUtil;

public class FastBowFix extends AntiGamingChair implements Listener {
	private Map<String, Integer> timesFired = null;
	
	public FastBowFix() {
		super("Fast Bow");
		timesFired = new HashMap<String, Integer>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(isEnabled()) {
			timesFired.clear();
		}
	}
	
	@EventHandler
	public void onEntityShootBow(EntityShootBowEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(PerformanceHandler.getPing(player) < getMaxPing()) {
				Vector velocity = event.getProjectile().getVelocity();
				double x = velocity.getX() < 0 ? velocity.getX() * -1 : velocity.getX();
				double z = velocity.getZ() < 0 ? velocity.getZ() * -1 : velocity.getZ();
				if((x + z) >= 2.75 && notIgnored(player)) {
					int times = 0;
					if(timesFired.containsKey(player.getName())) {
						times = timesFired.get(player.getName());
					}
					if(++times >= 2) {
						for(Player online : Bukkit.getOnlinePlayers()) {
							if(Ranks.HELPER.hasRank(online)) {
								MessageHandler.sendMessage(online, AccountHandler.getPrefix(player) + " &cis possibly using &eFast Bow");
							}
						}
						event.setCancelled(true);
					} else {
						timesFired.put(player.getName(), times);
					}
				}
			} else {
				timesFired.remove(player.getName());
			}
		}
	}
}
