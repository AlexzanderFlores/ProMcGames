package promcgames.anticheat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import promcgames.server.DB;
import promcgames.server.PerformanceHandler;
import promcgames.server.ProMcGames;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

public class ToggleSneak extends AntiGamingChair implements Listener {
	public ToggleSneak() {
		super("Toggle Sneak");
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			if(player.isSneaking()) {
				final String name = player.getName();
				final int ping = PerformanceHandler.getPing(player);
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						DB.NETWORK_ANTI_CHEAT_TESTING.insert("'" + name + "-" + ping + "-" + ProMcGames.getServerName() + "'");
					}
				});
				player.setSneaking(false);
			}
		}
	}
	
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if(isEnabled()) {
			Player player = event.getPlayer();
			if(player.isSneaking()) {
				final String name = player.getName();
				final int ping = PerformanceHandler.getPing(player);
				new AsyncDelayedTask(new Runnable() {
					@Override
					public void run() {
						DB.NETWORK_ANTI_CHEAT_TESTING.insert("'" + name + "-" + ping + "-" + ProMcGames.getServerName() + "'");
					}
				});
				player.setSneaking(false);
			}
		}
	}
}
