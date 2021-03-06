package promcgames.gameapi.games.survivalgames.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.PlayerHeadshotEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class BoomHeadshot1 extends Trophy {
	private static BoomHeadshot1 instance = null;
	private int amount = 3;
	
	public BoomHeadshot1() {
		super(Plugins.SURVIVAL_GAMES, 37);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static BoomHeadshot1 getInstance() {
		if(instance == null) {
			new BoomHeadshot1();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.ARROW).setName("&aBoom Headshot! 1").addLore("&eGet &c" + amount + " &eheadshots in one game").getItemStack();
	}
	
	@EventHandler
	public void onPlayerHeadshotEvent(PlayerHeadshotEvent event) {
		if(event.getNumberOfHeadshots() == amount) {
			setAchieved(event.getPlayer());
		}
	}
}
