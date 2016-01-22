package promcgames.gameapi.games.versus.trophies;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import promcgames.ProMcGames.Plugins;
import promcgames.gameapi.games.versus.events.BattleEndEvent;
import promcgames.player.trophies.Trophy;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class KohiChampion extends Trophy {
	private static KohiChampion instance = null;
	private int amount = 25;
	
	public KohiChampion() {
		super(Plugins.VERSUS, 37);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static KohiChampion getInstance() {
		if(instance == null) {
			new KohiChampion();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(new Potion(PotionType.INSTANT_HEAL, 1, true).toItemStack(1)).setName("&aKohi Champion").addLore("&eWin &c" + amount + " &eKohi Kit matches").getItemStack();
	}
	
	@EventHandler
	public void onBattleEnd(BattleEndEvent event) {
		if(event.getWinner() != null && event.getKit().getName().equals("Kohi")) {
			final UUID uuid = event.getWinner().getUniqueId();
			new AsyncDelayedTask(new Runnable() {
				@Override
				public void run() {
					String [] keys = new String [] {"uuid", "kit"};
					String [] values = new String [] {uuid.toString(), "Kohi"};
					if(DB.PLAYERS_VERSUS_STATS.getInt(keys, values, "wins") == amount) {
						setAchieved(Bukkit.getPlayer(uuid));
					}
				}
			});
		}
	}
}
