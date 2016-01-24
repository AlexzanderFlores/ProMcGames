package promcgames.gameapi.games.versus.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.gameapi.games.versus.events.KillstreakReachedEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class KillstreakSeeker3 extends Trophy {
	private static KillstreakSeeker3 instance = null;
	private int amount = 30;
	
	public KillstreakSeeker3() {
		super(Plugins.VERSUS, 12);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static KillstreakSeeker3 getInstance() {
		if(instance == null) {
			new KillstreakSeeker3();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.DIAMOND).setAmount(3).setName("&aKillstreak Seeker 3").addLore("&eGet a killstreak of &c" + amount).getItemStack();
	}
	
	@EventHandler
	public void onKillstreakReached(KillstreakReachedEvent event) {
		if(event.getStreak() == amount) {
			setAchieved(event.getPlayer());
		}
	}
}
