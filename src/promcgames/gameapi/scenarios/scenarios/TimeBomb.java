package promcgames.gameapi.scenarios.scenarios;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.gameapi.scenarios.Scenario;

public class TimeBomb extends Scenario {
	private static TimeBomb instance = null;
	private Map<Location, Integer> counters = null;
	
	public TimeBomb() {
		super("TimeBomb", Material.TNT);
		instance = this;
		setInfo("Whenever a player dies their items are placed in a chest at their death location. Chests explode after 30 seconds of being placed.");
		counters = new HashMap<Location, Integer>();
	}
	
	public static TimeBomb getInstance() {
		if(instance == null) {
			new TimeBomb();
		}
		return instance;
	}
	
	@EventHandler
	public void PlayerDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Location location = player.getLocation().add(0, -1, 0);
		location.getBlock().setType(Material.CHEST);
		Chest chestOne = (Chest) location.getBlock().getState();
		for(ItemStack item : player.getInventory().getContents()) {
			if(item != null) {
				chestOne.getInventory().addItem(item);
				player.getInventory().remove(item);
			}
		}
		Location nearLocation = location.add(1, 0, 0);
		nearLocation.getBlock().setType(Material.CHEST);
		Chest chestTwo = (Chest) nearLocation.getBlock().getState();
		for(ItemStack item : player.getInventory().getContents()) {
			if(item != null) {
				chestOne.getInventory().addItem(item);
				player.getInventory().remove(item);
			}
		}
		for(ItemStack armorItem : player.getInventory().getArmorContents()) {
			if(armorItem != null) {
				chestTwo.getInventory().addItem(armorItem);
			}
		}
		counters.put(location, 0);
		counters.put(nearLocation, 0);
		event.setKeepInventory(true);
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		Iterator<Location> iterator = counters.keySet().iterator();
		while(iterator.hasNext()) {
			Location location = iterator.next();
			int counter = 0;
			if(counters.containsKey(location)) {
				counter = counters.get(location);
			}
			if(++counter >= 30) {
				if(location.getBlock().getType() == Material.CHEST) {
					Chest chest = (Chest) location.getBlock().getState();
					chest.getInventory().clear();
				}
				location.getWorld().createExplosion(location, 4.0f);
				iterator.remove();
			} else {
				counters.put(location, counter);
			}
		}
	}
}
