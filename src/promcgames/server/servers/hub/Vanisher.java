package promcgames.server.servers.hub;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.WaterSplashEvent;
import promcgames.player.account.AccountHandler.Ranks;
import promcgames.server.util.EventUtil;

public class Vanisher implements Listener {
	private int requireEnable = 60;
	private int requireDisable = 50;
	private boolean enabled = false;
	
	public Vanisher() {
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(Bukkit.getOnlinePlayers().size() >= requireEnable && !enabled) {
			enabled = true;
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!Ranks.PRO.hasRank(player)) {
					for(Player player2 : Bukkit.getOnlinePlayers()) {
						player2.hidePlayer(player);
					}
				}
			}
		}
		if(enabled) {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!Ranks.PRO.hasRank(event.getPlayer())) {
					player.hidePlayer(event.getPlayer());
				}
				if(!Ranks.PRO.hasRank(player)) {
					event.getPlayer().hidePlayer(player);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(Bukkit.getOnlinePlayers().size() - 1 <= requireDisable && enabled) {
			enabled = false;
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(!Ranks.PRO.hasRank(player)) {
					for(Player player2 : Bukkit.getOnlinePlayers()) {
						player2.showPlayer(player);
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onWaterSplash(WaterSplashEvent event) {
		if(enabled) {
			event.setCancelled(true);
		}
	}
}
