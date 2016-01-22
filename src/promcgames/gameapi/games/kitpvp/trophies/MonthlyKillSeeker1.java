package promcgames.gameapi.games.kitpvp.trophies;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.customevents.player.StatsChangeEvent;
import promcgames.customevents.player.StatsChangeEvent.StatsType;
import promcgames.gameapi.StatsHandler;
import promcgames.player.trophies.Trophy;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class MonthlyKillSeeker1 extends Trophy {
	private static MonthlyKillSeeker1 instance = null;
	private int amount = 1000;
	
	public MonthlyKillSeeker1() {
		super(Plugins.KIT_PVP, 32);
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
		return new ItemCreator(Material.STONE_SWORD).setName("&aMonthly Kill Seeker 1").addLore("&eGet &c" + amount + " &emonthly kills").getItemStack();
	}
	
	@EventHandler
	public void onStatsChangeEvent(StatsChangeEvent event) {
		if(event.getType() == StatsType.KILL && StatsHandler.getMonthlyKills(event.getPlayer()) == (amount - 1)) {
			setAchieved(event.getPlayer());
		}
	}
}
