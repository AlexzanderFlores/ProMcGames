package promcgames.gameapi.games.skywars.trophies.teams;

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

public class MonthlyKillSeeker1 extends Trophy {
	private static MonthlyKillSeeker1 instance = null;
	private int requiredKills = 25;
	
	public MonthlyKillSeeker1() {
		super(Plugins.SKY_WARS_TEAMS, 16);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static MonthlyKillSeeker1 getInstance() {
		if(instance == null) {
			new MonthlyKillSeeker1();
		}
		return instance;
	}

	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.IRON_SWORD).setAmount(1).setName("&aMonthly Kill Hunter 1").
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
