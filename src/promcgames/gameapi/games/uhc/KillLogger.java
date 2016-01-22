package promcgames.gameapi.games.uhc;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import promcgames.ProMcGames;
import promcgames.ProPlugin;
import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.Disguise;
import promcgames.player.account.AccountHandler;
import promcgames.server.util.EventUtil;

public class KillLogger implements Listener {
	private Map<String, Integer> kills = null;
	
	public KillLogger() {
		kills = new HashMap<String, Integer>();
		if(HostedEvent.isEvent()) {
			ProMcGames.getSidebar().setText(new String [] {
				" ",
				"&cNo Deaths",
				"  ",
				"&5Elite UHC"
			});
		} else {
			ProMcGames.getSidebar().setText(new String [] {
				" ",
				"&cNo Deaths",
				"  "
			});
		}
		EventUtil.register(this);
	}
	
	private static String getText(Player player) {
		String text = AccountHandler.getRank(player).getColor() + Disguise.getName(player);
		if(text.length() > 16) {
			text = text.substring(0, 16);
		}
		return text;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(kills.containsKey(player.getName())) {
			ProMcGames.getSidebar().setText(getText(player), kills.get(player.getName()));
		}
		ProMcGames.getSidebar().setText("Playing", ProPlugin.getPlayers().size());
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		if(kills.isEmpty()) {
			ProMcGames.getSidebar().removeScoresBelow(0);
			if(HostedEvent.isEvent()) {
				ProMcGames.getSidebar().setText(new String [] {
					" ",
					"&5Elite UHC"
				});
			}
		}
		Player player = event.getPlayer();
		kills.remove(player.getName());
		ProMcGames.getSidebar().removeText(getText(player));
		Player killer = event.getKiller();
		if(killer == null) {
			String name = "PVE";
			if(kills.containsKey(name)) {
				kills.put(name, kills.get(name) + 1);
			} else {
				kills.put(name, 1);
			}
			ProMcGames.getSidebar().setText("&6" + name, kills.get(name));
		} else {
			if(kills.containsKey(killer.getName())) {
				kills.put(killer.getName(), kills.get(killer.getName()) + 1);
			} else {
				kills.put(killer.getName(), 1);
			}
			ProMcGames.getSidebar().setText(getText(killer), kills.get(killer.getName()));
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		ProMcGames.getSidebar().removeText(getText(player));
		ProMcGames.getSidebar().setText("Playing", ProPlugin.getPlayers().size());
	}
}
