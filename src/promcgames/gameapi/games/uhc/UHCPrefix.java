package promcgames.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.timed.TenTickTaskEvent;
import promcgames.server.DB;
import promcgames.server.ProMcGames;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class UHCPrefix implements Listener {
	private List<UUID> hosts = null;
	private List<UUID> queue = null;
	
	public UHCPrefix() {
		hosts = new ArrayList<UUID>();
		queue = new ArrayList<UUID>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		queue.add(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onTenTickTask(TenTickTaskEvent event) {
		if(!queue.isEmpty()) {
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					UUID uuid = queue.get(0);
					Player player = Bukkit.getPlayer(uuid);
					if(player != null && DB.NETWORK_UHC_HOSTS.isUUIDSet(uuid)) {
						hosts.add(uuid);
					}
					queue.remove(0);
				}
			});
		}
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		UUID uuid = event.getPlayer().getUniqueId();
		if(hosts.contains(uuid)) {
			if(ProMcGames.getPlugin() == Plugins.UHC) {
				Player mainHost = HostHandler.getMainHost();
				if(mainHost != null && mainHost.getUniqueId() == uuid) {
					return;
				}
			}
			event.setFormat(ChatColor.YELLOW + "[UHC] " + event.getFormat());
		}
	}
}
