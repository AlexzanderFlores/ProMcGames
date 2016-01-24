package promcgames.gameapi.games.kitpvp.trophies;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.GameDeathEvent;
import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class Revenge extends Trophy {
	private static Revenge instance = null;
	private Map<String, String> lastKilled = null; // killed, killer
	
	public Revenge() {
		super(Plugins.KIT_PVP, 16);
		instance = this;
		if(canRegister()) {
			lastKilled = new HashMap<String, String>();
			EventUtil.register(this);
		}
	}
	
	public static Revenge getInstance() {
		if(instance == null) {
			new Revenge();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.APPLE).setName("&aRevenge").addLore("&eKill the player who just killed you").getItemStack();
	}
	
	@EventHandler
	public void onGameDeath(GameDeathEvent event) {
		Player killer = event.getKiller();
		if(killer != null) {
			Player killed = event.getPlayer();
			if(killed != null) {
				lastKilled.put(killed.getName(), killer.getName());
				if(lastKilled.containsKey(killer.getName()) && lastKilled.get(killer.getName()).equals(killed.getName())) {
					setAchieved(killer);
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		lastKilled.remove(event.getPlayer().getName());
	}
}
