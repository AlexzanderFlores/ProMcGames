package promcgames.gameapi.games.uhc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import promcgames.ProMcGames;
import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.player.PlayerSpectateStartEvent;
import promcgames.customevents.player.PostPlayerJoinEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.MiniGame.GameStates;
import promcgames.gameapi.SpectatorHandler;
import promcgames.gameapi.games.uhc.events.PlayerTimeOutEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.player.account.AccountHandler;
import promcgames.server.util.EventUtil;

public class DisconnectHandler implements Listener {
	private static Map<UUID, Integer> times = null;
	private static List<String> cannotRelog = null;
	
	public DisconnectHandler() {
		times = new HashMap<UUID, Integer>();
		cannotRelog = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	public static boolean isDisconnected(Player player) {
		return times.containsKey(player.getUniqueId());
	}
	
	public static boolean cannotRelog(Player player) {
		return cannotRelog.contains(player.getName());
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(!HostedEvent.isEvent()) {
			Iterator<UUID> iterator = times.keySet().iterator();
			while(iterator.hasNext()) {
				UUID uuid = iterator.next();
				times.put(uuid, times.get(uuid) + 1);
				if(times.get(uuid) >= (60 * 5)) {
					Bukkit.getPluginManager().callEvent(new PlayerTimeOutEvent(uuid));
					iterator.remove();
					String name = AccountHandler.getName(uuid);
					cannotRelog.remove(name);
					WhitelistHandler.unWhitelist(uuid);
					MessageHandler.alert(name + " &ctook too long to come back!");
				}
			}
		}
	}
	
	@EventHandler
	public void onPostPlayerJoin(PostPlayerJoinEvent event) {
		times.remove(event.getPlayer().getUniqueId());
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		cannotRelog.add(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerSpectateStart(PlayerSpectateStartEvent event) {
		if(times.containsKey(Disguise.getUUID(event.getPlayer()))) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		Player player = event.getPlayer();
		if(ProMcGames.getMiniGame().getGameState() == GameStates.STARTED && !SpectatorHandler.contains(player) && !cannotRelog.contains(player.getName())) {
			times.put(Disguise.getUUID(event.getPlayer()), 0);
		}
	}
}
