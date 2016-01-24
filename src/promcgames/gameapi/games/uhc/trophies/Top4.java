package promcgames.gameapi.games.uhc.trophies;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.player.trophies.Trophy;
import promcgames.server.ProPlugin;
import promcgames.server.ProMcGames.Plugins;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class Top4 extends Trophy {
	private static Top4 instance = null;
	
	public Top4() {
		super(Plugins.UHC, 19);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static Top4 getInstance() {
		if(instance == null) {
			new Top4();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.DIAMOND_SWORD).setAmount(4).setName("&aTop 4").addLore("&eLast until the Top 4 players").getItemStack();
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				List<Player> players = ProPlugin.getPlayers();
				if(players.size() <= 4) {
					for(Player player : players) {
						setAchieved(player);
					}
				}
			}
		}, 20);
	}
}
