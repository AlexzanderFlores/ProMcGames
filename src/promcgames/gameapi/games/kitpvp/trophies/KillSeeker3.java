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

public class KillSeeker3 extends Trophy {
	private static KillSeeker3 instance = null;
	private int amount = 45000;
	
	public KillSeeker3() {
		super(Plugins.KIT_PVP, 21);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static KillSeeker3 getInstance() {
		if(instance == null) {
			new KillSeeker3();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.STONE_SWORD).setAmount(3).setName("&aKill Seeker 3").addLore("&eGet &c" + amount + " &elifetime kills").getItemStack();
	}
	
	@EventHandler
	public void onStatsChangeEvent(StatsChangeEvent event) {
		if(event.getType() == StatsType.KILL && StatsHandler.getKills(event.getPlayer()) == (amount - 1)) {
			setAchieved(event.getPlayer());
		}
	}
}
