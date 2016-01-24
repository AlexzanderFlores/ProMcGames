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

public class MonthlyKillSeeker2 extends Trophy {
	private static MonthlyKillSeeker2 instance = null;
	private int amount = 1500;
	
	public MonthlyKillSeeker2() {
		super(Plugins.KIT_PVP, 33);
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
		return new ItemCreator(Material.STONE_SWORD).setAmount(2).setName("&aMonthly Kill Seeker 2").addLore("&eGet &c" + amount + " &emonthly kills").getItemStack();
	}
	
	@EventHandler
	public void onStatsChangeEvent(StatsChangeEvent event) {
		if(event.getType() == StatsType.KILL && StatsHandler.getMonthlyKills(event.getPlayer()) == (amount - 1)) {
			setAchieved(event.getPlayer());
		}
	}
}
