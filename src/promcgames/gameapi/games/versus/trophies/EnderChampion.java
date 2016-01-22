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

public class EnderChampion extends Trophy {
	private static EnderChampion instance = null;
	private int amount = 25;
	
	public EnderChampion() {
		super(Plugins.VERSUS, 30);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static EnderChampion getInstance() {
		if(instance == null) {
			new EnderChampion();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.ENDER_PEARL).setName("&aEnder Champion").addLore("&eWin &c" + amount + " &eEnder Kit matches").getItemStack();
	}
	
	@EventHandler
	public void onBattleEnd(BattleEndEvent event) {
		if(event.getWinner() != null && event.getKit().getName().equals("Ender")) {
			final UUID uuid = event.getWinner().getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String [] keys = new String [] {"uuid", "kit"};
					String [] values = new String [] {uuid.toString(), "Ender"};
					if(DB.PLAYERS_VERSUS_STATS.getInt(keys, values, "wins") == amount) {
						setAchieved(Bukkit.getPlayer(uuid));
					}
				}
			});
		}
	}
}
