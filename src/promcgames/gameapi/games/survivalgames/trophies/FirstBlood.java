package promcgames.gameapi.games.survivalgames.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameKillEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class FirstBlood extends Trophy {
	private static FirstBlood instance = null;
	private boolean done = false;
	
	public FirstBlood() {
		super(Plugins.SURVIVAL_GAMES, 15);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static FirstBlood getInstance() {
		if(instance == null) {
			new FirstBlood();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.REDSTONE).setName("&aFirst Blood").addLore("&eGet the first kill of a game").getItemStack();
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		if(!done) {
			done = true;
			setAchieved(event.getPlayer());
		}
	}
}
