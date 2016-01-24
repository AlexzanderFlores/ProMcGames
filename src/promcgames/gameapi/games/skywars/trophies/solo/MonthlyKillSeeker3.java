package promcgames.gameapi.games.skywars.trophies.solo;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.game.GameKillEvent;
import promcgames.gameapi.StatsHandler;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class MonthlyKillSeeker3 extends Trophy {
	private static MonthlyKillSeeker3 instance = null;
	private int requiredKills = 100;
	
	public MonthlyKillSeeker3() {
		super(Plugins.SKY_WARS, 20);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static MonthlyKillSeeker3 getInstance() {
		if(instance == null) {
			new MonthlyKillSeeker3();
		}
		return instance;
	}

	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.IRON_SWORD).setAmount(3).setName("&aMonthly Kill Hunter 3").
				addLore("&eGet &c" + requiredKills + " &emonthly game kills").getItemStack();
	}
	
	@EventHandler
	public void onGameKill(GameKillEvent event) {
		Player player = event.getPlayer();
		if(StatsHandler.getKills(player) >= requiredKills) {
			setAchieved(player);
		}
	}
}
