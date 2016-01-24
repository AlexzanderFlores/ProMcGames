package promcgames.gameapi.games.survivalgames.trophies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameKillEvent;
import promcgames.customevents.game.GameStartEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.games.survivalgames.kits.premium.ResurrectionKit;
import promcgames.player.Disguise;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class DoubleKillChallenge extends Trophy {
	private static DoubleKillChallenge instance = null;
	private Map<String, List<String>> killed = null;
	
	public DoubleKillChallenge() {
		super(Plugins.SURVIVAL_GAMES, 41);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static DoubleKillChallenge getInstance() {
		if(instance == null) {
			new DoubleKillChallenge();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.GOLD_NUGGET).setName("&aDouble Kill Challenge").setLores(new String [] {
			"&eKill the same player twice in one game",
			"&7(Requires Resurrection Kit)"
		}).getItemStack();
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		if(ResurrectionKit.used) {
			killed = new HashMap<String, List<String>>();
		}
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		if(killed != null) {
			List<String> kills = new ArrayList<String>();
			if(killed.containsKey(Disguise.getName(event.getPlayer()))) {
				kills = killed.get(Disguise.getName(event.getPlayer()));
			}
			if(kills.contains(Disguise.getName(event.getKilled()))) {
				setAchieved(event.getPlayer());
			}
			kills.add(Disguise.getName(event.getKilled()));
			killed.put(Disguise.getName(event.getPlayer()), kills);
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		if(killed != null && killed.containsKey(Disguise.getName(event.getPlayer()))) {
			killed.get(Disguise.getName(event.getPlayer())).clear();
			killed.remove(Disguise.getName(event.getPlayer()));
		}
	}
}
