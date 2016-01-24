package promcgames.gameapi.games.survivalgames.trophies;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.GameWinEvent;
import promcgames.gameapi.StatsHandler;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class VictoryHunter5 extends Trophy {
	private static VictoryHunter5 instance = null;
	private int requiredWins = 100;
	
	public VictoryHunter5() {
		super(Plugins.SURVIVAL_GAMES, 24);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static VictoryHunter5 getInstance() {
		if(instance == null) {
			new VictoryHunter5();
		}
		return instance;
	}

	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.DIAMOND_SWORD).setAmount(5).setName("&aVictory Hunter 5").
				addLore("&eGet &c" + requiredWins + " &egame wins").getItemStack();
	}
	
	@EventHandler
	public void onGameWin(GameWinEvent event) {
		Player player = event.getPlayer();
		if(StatsHandler.getWins(player) >= requiredWins) {
			setAchieved(player);
		}
	}
}
