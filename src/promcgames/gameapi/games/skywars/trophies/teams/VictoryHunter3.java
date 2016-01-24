package promcgames.gameapi.games.skywars.trophies.teams;

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

public class VictoryHunter3 extends Trophy {
	private static VictoryHunter3 instance = null;
	private int requiredWins = 25;
	
	public VictoryHunter3() {
		super(Plugins.SKY_WARS_TEAMS, 13);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static VictoryHunter3 getInstance() {
		if(instance == null) {
			new VictoryHunter3();
		}
		return instance;
	}

	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.IRON_CHESTPLATE).setAmount(3).setName("&aVictory Hunter 3").
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
