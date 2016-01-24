package promcgames.gameapi.games.skywars.trophies.solo;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.game.GameKillEvent;
import promcgames.gameapi.StatsHandler;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class MonthlyKillSeeker2 extends Trophy {
	private static MonthlyKillSeeker2 instance = null;
	private int requiredKills = 50;
	
	public MonthlyKillSeeker2() {
		super(Plugins.SKY_WARS, 19);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static MonthlyKillSeeker2 getInstance() {
		if(instance == null) {
			new MonthlyKillSeeker2();
		}
		return instance;
	}

	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.IRON_SWORD).setAmount(2).setName("&aMonthly Kill Hunter 2").
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
