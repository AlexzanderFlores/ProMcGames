package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.games.kitpvp.events.KillstreakEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class ExtraHealth extends Trophy {
	private static ExtraHealth instance = null;
	
	public ExtraHealth() {
		super(Plugins.KIT_PVP, 23);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static ExtraHealth getInstance() {
		if(instance == null) {
			new ExtraHealth();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.GOLDEN_APPLE).setName("&aExtra Health").addLore("&eGet the Extra Health killstreak").getItemStack();
	}
	
	@EventHandler
	public void onKillstreak(KillstreakEvent event) {
		if(getName().equals(event.getKillstreak().getName())) {
			setAchieved(event.getPlayer());
		}
	}
}
