package promcgames.gameapi.games.versus.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.gameapi.games.versus.events.KillstreakReachedEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class KillstreakSeeker1 extends Trophy {
	private static KillstreakSeeker1 instance = null;
	private int amount = 10;
	
	public KillstreakSeeker1() {
		super(Plugins.VERSUS, 10);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static KillstreakSeeker1 getInstance() {
		if(instance == null) {
			new KillstreakSeeker1();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.DIAMOND).setName("&aKillstreak Seeker 1").addLore("&eGet a killstreak of &c" + amount).getItemStack();
	}
	
	@EventHandler
	public void onKillstreakReached(KillstreakReachedEvent event) {
		if(event.getStreak() == amount) {
			setAchieved(event.getPlayer());
		}
	}
}
