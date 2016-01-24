package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.gameapi.games.kitpvp.events.KillstreakEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class SnowballFight extends Trophy {
	private static SnowballFight instance = null;
	
	public SnowballFight() {
		super(Plugins.KIT_PVP, 30);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static SnowballFight getInstance() {
		if(instance == null) {
			new SnowballFight();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.SNOW_BALL).setName("&aSnowball Fight").addLore("&eGet the Snowball Fight killstreak").getItemStack();
	}
	
	@EventHandler
	public void onKillstreak(KillstreakEvent event) {
		if(getName().equals(event.getKillstreak().getName())) {
			setAchieved(event.getPlayer());
		}
	}
}
