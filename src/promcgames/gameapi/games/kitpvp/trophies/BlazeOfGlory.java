package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class BlazeOfGlory extends Trophy {
	private static BlazeOfGlory instance = null;
	
	public BlazeOfGlory() {
		super(Plugins.KIT_PVP, 15);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static BlazeOfGlory getInstance() {
		if(instance == null) {
			new BlazeOfGlory();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.FIRE).setName("&aBlaze of Glory").addLore("&eKill someone while they're on fire").getItemStack();
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if(player.getKiller() != null) {
			Player killer = player.getKiller();
			if(player.getFireTicks() > 0) {
				setAchieved(killer);
			}
		}
	}
}
