package promcgames.gameapi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProPlugin;
import promcgames.customevents.game.PlayerExpFillEvent_;
import promcgames.customevents.player.PlayerSpectateStartEvent;
import promcgames.customevents.timed.OneTickTaskEvent;
import promcgames.player.Disguise;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;

public class PlayerExpGainer implements Listener {
	private static List<String> players = null;
	
	public PlayerExpGainer() {
		players = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	public static void start(Player player) {
		if(players == null) {
			new PlayerExpGainer();
		}
		players.add(Disguise.getName(player));
	}
	
	public static void stop(Player player) {
		if(players != null) {
			players.remove(Disguise.getName(player));
		}
		player.setExp(0.0f);
	}
	
	@EventHandler
	public void onOneTickTask(OneTickTaskEvent event) {
		if(players != null && !players.isEmpty()) {
			Iterator<String> list = players.iterator();
			while(list.hasNext()) {
				Player player = ProPlugin.getPlayer(list.next());
				if(player == null) {
					list.remove();
				} else {
					player.setExp(player.getExp() + 0.005f);
					if(player.getExp() >= 1.0f) {
						list.remove();
						player.setExp(0.0f);
						EffectUtil.playSound(player, Sound.LEVEL_UP);
						Bukkit.getPluginManager().callEvent(new PlayerExpFillEvent_(player));
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerSpectateStart(PlayerSpectateStartEvent event) {
		stop(event.getPlayer());
	}
}
