package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.games.kitpvp.events.KillstreakEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class Speed extends Trophy {
	private static Speed instance = null;
	
	public Speed() {
		super(Plugins.KIT_PVP, 25);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static Speed getInstance() {
		if(instance == null) {
			new Speed();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.DIAMOND_BOOTS).setName("&aSpeed").addLore("&eGet the Speed killstreak").getItemStack();
	}
	
	@EventHandler
	public void onKillstreak(KillstreakEvent event) {
		if(getName().equals(event.getKillstreak().getName())) {
			setAchieved(event.getPlayer());
		}
	}
}
