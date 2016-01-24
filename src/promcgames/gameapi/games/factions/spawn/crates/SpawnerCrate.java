package promcgames.gameapi.games.factions.spawn.crates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import promcgames.ProPlugin;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.player.MessageHandler;
import promcgames.server.DB;
import promcgames.server.tasks.AsyncDelayedTask;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.ItemCreator;
import promcgames.server.util.StringUtil;

public class SpawnerCrate extends CrateBase {
	private Map<String, String> correctNames = null; // type name, correct name
	private List<String> types = null;
	private int changeTypesCounter = 0;
	
	public SpawnerCrate() {
		super(DB.PLAYERS_FACTIONS_SPAWNER_CRATES, new Location(Bukkit.getWorlds().get(0), -230, 67, 278), "&cYou do not have any Spawner Crate keys! &b/buy");
		correctNames = new HashMap<String, String>();
		types = new ArrayList<String>();
		correctNames.put("LavaSlime", "Magma Cube");
		correctNames.put("EntityHorse", "Horse");
		correctNames.put("Ozelot", "Ocelot");
		correctNames.put("VillagerGolem", "Iron Golem");
		types.add("MushroomCow");
		types.add("Slime");
		types.add("LavaSlime");
		types.add("Spider");
		types.add("CaveSpider");
		types.add("Pig");
		types.add("Chicken");
		types.add("EntityHorse");
		types.add("Ozelot");
		types.add("VillagerGolem");
		types.add("Witch");
		types.add("PigZombie");
		types.add("Sheep");
		types.add("Skeleton");
		types.add("Zombie");
		types.add("Creeper");
		types.add("Blaze");
		types.add("Enderman");
		types.add("Villager");
		hologram.appendTextLine(StringUtil.color("&bSpawner Crate"));
		hologram.appendTextLine(StringUtil.color("&eFound on &b/buy"));
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(changeTypesCounter > 0) {
			Block block = location.getBlock();
			String type = types.get(new Random().nextInt(types.size()));
			if(block.getType() == Material.MOB_SPAWNER) {
				CreatureSpawner spawner = (CreatureSpawner) block.getState();
				spawner.setCreatureTypeByName(type);
				spawner.update(true);
			}
			if(--changeTypesCounter <= 0) {
				Player player = ProPlugin.getPlayer(user);
				if(player != null) {
					delayUse = true;
					ItemStack spawner = new ItemCreator(Material.MOB_SPAWNER).setName(type + " Spawner").getItemStack();
					boolean gave = false;
					for(int a = 0; a < player.getInventory().getContents().length; ++a) {
						ItemStack item = player.getInventory().getItem(a);
						if(item == null || item.getType() == Material.AIR) {
							player.getInventory().addItem(spawner);
							gave = true;
							break;
						}
					}
					if(gave) {
						final UUID uuid = player.getUniqueId();
						new AsyncDelayedTask(new Runnable() {
							@Override
							public void run() {
								int amount = table.getInt("uuid", uuid.toString(), "amount") - 1;
								table.updateInt("amount", amount, "uuid", uuid.toString());
							}
						});
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								Block block = location.getBlock();
								if(block.getType() == Material.MOB_SPAWNER) {
									CreatureSpawner spawner = (CreatureSpawner) block.getState();
									spawner.setCreatureTypeByName("Pig");
									spawner.update(true);
								}
								delayUse = false;
							}
						}, 20 * 5);
						if(correctNames.containsKey(type)) {
							type = correctNames.get(type);
						}
						alert(player, type + " Spawner");
					} else {
						MessageHandler.sendMessage(player, "&cYou do not have enough room in your inventory for this item");
						MessageHandler.sendMessage(player, "You did &cNOT &alose your key. Please make room and try again");
					}
				}
				if(npc != null) {
					if(npc.getLivingEntity().getPassenger() != null) {
						npc.getLivingEntity().getPassenger().remove();
					}
					npc.remove();
					npc = null;
				}
				moveHologramDown();
				user = null;
				CrateBase.anyBeingUsed = false;
			}
		}
	}
	
	@Override
	public void interact(Player player) {
		super.interact(player);
		changeTypesCounter = 7;
		
	}
}
