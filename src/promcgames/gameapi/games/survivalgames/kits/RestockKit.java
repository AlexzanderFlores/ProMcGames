package promcgames.gameapi.games.survivalgames.kits;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.gameapi.games.survivalgames.SurvivalGames;
import promcgames.gameapi.games.survivalgames.events.RestockChestEvent;
import promcgames.gameapi.kits.KitBase;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.EventUtil;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.MathUtil;

public class RestockKit extends KitBase implements Listener {
	private Map<String, Integer> playerUsageCounters = null;
	private Map<String, List<Location>> playerUsedLocations = null;
	private static int amount = 5;
	private static int price = 500;
	
	public RestockKit() {
		super(new ItemCreator(Material.CHEST).setName("Restock").setLores(new String [] {
			"&bPrice: " + price,
			"",
			"&6Breaking a chest will restock it",
			"&e" + amount + " &6uses"
		}).getItemStack(), 16);
	}
	
	@Override
	public String getPermission() {
		return "survival_games.restock";
	}

	@Override
	public void execute() {
		if(!getPlayers().isEmpty()) {
			playerUsageCounters = new HashMap<String, Integer>();
			playerUsedLocations = new HashMap<String, List<Location>>();
			for(Player player : getPlayers()) {
				playerUsageCounters.put(Disguise.getName(player), amount);
			}
			EventUtil.register(this);
		}
	}

	@Override
	public void execute(Player player) {
		
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Material type = event.getBlock().getType();
		if(has(player) && (type == Material.CHEST || type == Material.TRAPPED_CHEST)) {
			if(playerUsageCounters.containsKey(Disguise.getName(player))) {
				Block block = event.getBlock();
				if(MathUtil.getDistance(SurvivalGames.arenaCenter, block.getLocation()) <= 30) {
					MessageHandler.sendMessage(player, "&cThis chest is too close to spawn to restock it");
				} else {
					List<Location> usedLocations = playerUsedLocations.get(Disguise.getName(player));
					if(usedLocations == null) {
						usedLocations = new ArrayList<Location>();
					}
					if(!usedLocations.contains(block.getLocation())) {
						usedLocations.add(block.getLocation());
						playerUsedLocations.put(Disguise.getName(player), usedLocations);
						playerUsageCounters.put(Disguise.getName(player), playerUsageCounters.get(Disguise.getName(player)) - 1);
						if(playerUsageCounters.get(Disguise.getName(player)) == 0) {
							playerUsageCounters.remove(Disguise.getName(player));
							MessageHandler.sendMessage(player, "&cYou have ran out of restock uses");
						} else {
							MessageHandler.sendMessage(player, "You have &c" + playerUsageCounters.get(Disguise.getName(player)) + "&a uses left");
						}
						EffectUtil.playEffect(Effect.MOBSPAWNER_FLAMES, block.getLocation());
						Chest chest = (Chest) block.getState();
						RestockChestEvent restockChestEvent = new RestockChestEvent(chest);
						Bukkit.getPluginManager().callEvent(restockChestEvent);
					}
				}
			} else {
				MessageHandler.sendMessage(player, "&cYou have ran out of restock uses");
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		playerUsageCounters.remove(event.getPlayer().getName());
		if(playerUsedLocations.containsKey(event.getPlayer().getName())) {
			playerUsedLocations.get(event.getPlayer().getName()).clear();
		}
		playerUsedLocations.remove(event.getPlayer().getName());
	}
}
