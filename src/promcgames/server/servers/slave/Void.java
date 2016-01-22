package promcgames.server.servers.slave;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.customevents.timed.OneTickTaskEvent;
import promcgames.server.util.EventUtil;

public class Void implements Listener {
	public Void() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onOneTickTask(OneTickTaskEvent event) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.playSound(player.getLocation(), Sound.ENDERDRAGON_DEATH, 10000.0f, 10000.0f);
			player.playSound(player.getLocation(), Sound.IRONGOLEM_DEATH, 10000.0f, 10000.0f);
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 10000.0f, 10000.0f);
			player.openInventory(Bukkit.createInventory(player, 9 * 6));
		}
	}
}
