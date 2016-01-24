package promcgames.gameapi.games.survivalgames;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProPlugin;
import promcgames.customevents.game.DeathmatchStartEvent;
import promcgames.customevents.game.GameEndingEvent;
import promcgames.customevents.game.GameStartEvent;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;

//UNTESTED & UNUSED
//Goal is to log games for website viewing
public class GameLogger implements Listener {
	private List<UUID> played = null;
	private List<UUID> DM = null;
	
	public GameLogger() {
		played = new ArrayList<UUID>();
		DM = new ArrayList<UUID>();
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			played.add(player.getUniqueId());
		}
	}
	
	@EventHandler
	public void onDeathmatchStart(DeathmatchStartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			DM.add(player.getUniqueId());
		}
	}
	
	@EventHandler
	public void onGameEnding(GameEndingEvent event) {
		new AsyncDelayedTask(new Runnable() {
			@Override
			public void run() {
				//TODO: Add the data to the database
			}
		});
	}
}
