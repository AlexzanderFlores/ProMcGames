package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.gameapi.games.kitpvp.events.KillstreakEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class Juggernaut extends Trophy {
	private static Juggernaut instance = null;
	
	public Juggernaut() {
		super(Plugins.KIT_PVP, 28);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static Juggernaut getInstance() {
		if(instance == null) {
			new Juggernaut();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.DIAMOND_CHESTPLATE).setName("&aJuggernaut").addLore("&eGet the Juggernaut killstreak").getItemStack();
	}
	
	@EventHandler
	public void onKillstreak(KillstreakEvent event) {
		if(getName().equals(event.getKillstreak().getName())) {
			setAchieved(event.getPlayer());
		}
	}
}
