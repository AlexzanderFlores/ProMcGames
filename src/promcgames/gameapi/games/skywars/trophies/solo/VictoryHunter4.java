package promcgames.gameapi.games.skywars.trophies.solo;

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

public class VictoryHunter4 extends Trophy {
	private static VictoryHunter4 instance = null;
	private int requiredWins = 50;
	
	public VictoryHunter4() {
		super(Plugins.SKY_WARS, 14);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static VictoryHunter4 getInstance() {
		if(instance == null) {
			new VictoryHunter4();
		}
		return instance;
	}

	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.IRON_CHESTPLATE).setAmount(4).setName("&aVictory Hunter 4").
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
