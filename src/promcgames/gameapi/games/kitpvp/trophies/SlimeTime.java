package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.gameapi.games.kitpvp.events.KillstreakEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class SlimeTime extends Trophy {
	private static SlimeTime instance = null;
	
	public SlimeTime() {
		super(Plugins.KIT_PVP, 31);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static SlimeTime getInstance() {
		if(instance == null) {
			new SlimeTime();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.SLIME_BALL).setName("&aSlime Time").addLore("&eGet the Slime Time killstreak").getItemStack();
	}
	
	@EventHandler
	public void onKillstreak(KillstreakEvent event) {
		if(getName().equals(event.getKillstreak().getName())) {
			setAchieved(event.getPlayer());
		}
	}
}
