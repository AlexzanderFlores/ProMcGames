package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.player.StatsChangeEvent;
import promcgames.customevents.player.StatsChangeEvent.StatsType;
import promcgames.gameapi.StatsHandler;
import promcgames.player.trophies.Trophy;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class MonthlyKillSeeker3 extends Trophy {
	private static MonthlyKillSeeker3 instance = null;
	private int amount = 2000;
	
	public MonthlyKillSeeker3() {
		super(Plugins.KIT_PVP, 34);
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
		return new ItemCreator(Material.STONE_SWORD).setAmount(3).setName("&aMonthly Kill Seeker 3").addLore("&eGet &c" + amount + " &emonthly kills").getItemStack();
	}
	
	@EventHandler
	public void onStatsChangeEvent(StatsChangeEvent event) {
		if(event.getType() == StatsType.KILL && StatsHandler.getMonthlyKills(event.getPlayer()) == (amount - 1)) {
			setAchieved(event.getPlayer());
		}
	}
}
