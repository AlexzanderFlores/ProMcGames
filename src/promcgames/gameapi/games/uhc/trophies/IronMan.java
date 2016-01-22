package promcgames.gameapi.games.uhc.trophies;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameStartEvent;
import promcgames.gameapi.SpectatorHandler;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class IronMan extends Trophy implements Listener {
	private static IronMan instance = null;
	private List<String> players = new ArrayList<String>();
	
	public IronMan() {
		super(Plugins.UHC, 21);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static IronMan getInstance() {
		if(instance == null) {
			new IronMan();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.GOLDEN_APPLE).setName("&aIron Man").addLore("&eBe the last player with full health").getItemStack();
	}
	
	@EventHandler
	public void onGameStart(GameStartEvent event) {
		for(Player player : ProPlugin.getPlayers()) {
			players.add(player.getName());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageEvent event) {
		if(!event.isCancelled() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if(!SpectatorHandler.contains(player)) {
				players.remove(player.getName());
				if(players.size() == 1) {
					HandlerList.unregisterAll(this);
					player = ProPlugin.getPlayer(players.get(0));
					if(player != null) {
						setAchieved(player);
						players.clear();
						players = null;
					}
				}
			}
		}
	}
}
