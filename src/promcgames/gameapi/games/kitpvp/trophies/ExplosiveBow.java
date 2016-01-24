package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.gameapi.games.kitpvp.events.KillstreakEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class ExplosiveBow extends Trophy {
	private static ExplosiveBow instance = null;
	
	public ExplosiveBow() {
		super(Plugins.KIT_PVP, 29);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static ExplosiveBow getInstance() {
		if(instance == null) {
			new ExplosiveBow();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.BOW).setName("&aExplosive Bow").addLore("&eGet the Explosive Bow killstreak").getItemStack();
	}
	
	@EventHandler
	public void onKillstreak(KillstreakEvent event) {
		if(getName().equals(event.getKillstreak().getName())) {
			setAchieved(event.getPlayer());
		}
	}
}
