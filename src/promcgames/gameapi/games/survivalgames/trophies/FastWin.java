package promcgames.gameapi.games.survivalgames.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameWinEvent;
import promcgames.gameapi.games.survivalgames.deathmatch.DeathmatchHandler;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class FastWin extends Trophy {
	private static FastWin instance = null;
	
	public FastWin() {
		super(Plugins.SURVIVAL_GAMES, 12);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static FastWin getInstance() {
		if(instance == null) {
			new FastWin();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.FIREWORK).setName("&aFast Win").addLore("&eWin the game before deathmatch").getItemStack();
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		if(!DeathmatchHandler.isRunning()) {
			setAchieved(event.getPlayer());
		}
	}
}
