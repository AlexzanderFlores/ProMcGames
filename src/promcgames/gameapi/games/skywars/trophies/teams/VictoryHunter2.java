package promcgames.gameapi.games.skywars.trophies.teams;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameWinEvent;
import promcgames.gameapi.StatsHandler;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class VictoryHunter2 extends Trophy {
	private static VictoryHunter2 instance = null;
	private int requiredWins = 10;
	
	public VictoryHunter2() {
		super(Plugins.SKY_WARS_TEAMS, 12);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static VictoryHunter2 getInstance() {
		if(instance == null) {
			new VictoryHunter2();
		}
		return instance;
	}

	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.IRON_CHESTPLATE).setAmount(2).setName("&aVictory Hunter 2").
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
