package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.games.kitpvp.events.KillstreakEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class PoisonBow extends Trophy {
	private static PoisonBow instance = null;
	
	public PoisonBow() {
		super(Plugins.KIT_PVP, 22);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static PoisonBow getInstance() {
		if(instance == null) {
			new PoisonBow();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.BOW).setName("&aPoison Bow").addLore("&eGet the Poison Bow killstreak").getItemStack();
	}
	
	@EventHandler
	public void onKillstreak(KillstreakEvent event) {
		if(getName().equals(event.getKillstreak().getName())) {
			setAchieved(event.getPlayer());
		}
	}
}
