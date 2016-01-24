package promcgames.gameapi.games.uhc.trophies;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.ProPlugin;
import promcgames.ProMcGames.Plugins;
import promcgames.player.trophies.Trophy;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;

public class Top2 extends Trophy {
	private static Top2 instance = null;
	
	public Top2() {
		super(Plugins.UHC, 20);
		instance = this;
		if(canRegister()) {
			EventUtil.register(this);
		}
	}
	
	public static Top2 getInstance() {
		if(instance == null) {
			new Top2();
		}
		return instance;
	}
	
	@Override
	public ItemStack getIcon() {
		return new ItemCreator(Material.DIAMOND_SWORD).setAmount(2).setName("&aTop 2").addLore("&eLast until the Top 2 players").getItemStack();
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		new DelayedTask(new Runnable() {
			@Override
			public void run() {
				List<Player> players = ProPlugin.getPlayers();
				if(players.size() <= 2) {
					for(Player player : players) {
						setAchieved(player);
					}
				}
			}
		}, 20);
	}
}
