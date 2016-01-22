package promcgames.staff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.player.PlayerBanEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.StatsHandler;
import promcgames.player.MessageHandler;
import promcgames.server.util.EventUtil;

public class KillLogger implements Listener {
	private Map<UUID, List<UUID>> kills = null;
	
	public KillLogger() {
		kills = new HashMap<UUID, List<UUID>>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		Player killer = event.getKiller();
		if(killer != null) {
			Player player = event.getPlayer();
			List<UUID> killers = kills.get(player.getUniqueId());
			if(killers == null) {
				killers = new ArrayList<UUID>();
			}
			killers.add(killer.getUniqueId());
			kills.put(player.getUniqueId(), killers);
		}
	}
	
	@EventHandler
	public void onPlayerBan(PlayerBanEvent event) {
		for(UUID uuid : kills.keySet()) {
			for(UUID killer : kills.get(uuid)) {
				if(event.getUUID() == killer) {
					Player killed = Bukkit.getPlayer(uuid);
					if(killed != null) {
						StatsHandler.removeDeath(killed);
						MessageHandler.sendMessage(killed, "Removed &c1 &adeath due to being killed by a hacker");
					}
				}
			}
			kills.get(uuid).remove(event.getUUID());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(kills.containsKey(player.getUniqueId())) {
			kills.get(player.getUniqueId()).clear();
			kills.remove(player.getUniqueId());
		}
	}
}
