package promcgames.gameapi.games.versus.trophies;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProMcGames.Plugins;
import promcgames.gameapi.games.versus.events.BattleEndEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class IronChampion extends Trophy {
	private static IronChampion instance = null;
	private int amount = 25;
	
	public IronChampion() {
		super(Plugins.VERSUS, 21);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static IronChampion getInstance() {
		if(instance == null) {
			new IronChampion();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.IRON_HELMET).setName("&aIron Champion").addLore("&eWin &c" + amount + " &eIron Kit matches").getItemStack();
	}
	
	@EventHandler
	public void onBattleEnd(BattleEndEvent event) {
		if(event.getWinner() != null && event.getKit().getName().equals("Iron")) {
			final UUID uuid = event.getWinner().getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String [] keys = new String [] {"uuid", "kit"};
					String [] values = new String [] {uuid.toString(), "Iron"};
					if(DB.PLAYERS_VERSUS_STATS.getInt(keys, values, "wins") == amount) {
						setAchieved(Bukkit.getPlayer(uuid));
					}
				}
			});
		}
	}
}
