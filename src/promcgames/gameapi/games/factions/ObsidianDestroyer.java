package promcgames.gameapi.games.factions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import promcgames.customevents.ServerRestartEvent;
import promcgames.server.ProMcGames;
import promcgames.server.nms.npcs.NPCEntity;
import promcgames.server.util.EventUtil;
import promcgames.server.util.StringUtil;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;

public class ObsidianDestroyer implements Listener {
	private Map<Block, Integer> counters = null;
	private int required = 3;
	private Item item = null;
	
	public ObsidianDestroyer() {
		counters = new HashMap<Block, Integer>();
		LivingEntity livingEntity = new NPCEntity(EntityType.COW, null, new Location(Bukkit.getWorlds().get(0), -254.5, 69, 292.5)) {
			@Override
			public void onInteract(Player player) {
				
			}
		}.getLivingEntity();
		livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100));
		item = livingEntity.getWorld().dropItem(livingEntity.getLocation(), new ItemStack(Material.OBSIDIAN));
		livingEntity.setPassenger(item);
		Hologram hologram = HologramsAPI.createHologram(ProMcGames.getInstance(), livingEntity.getLocation().add(0, 2.5, 0));
		hologram.appendTextLine(StringUtil.color("&5Obsidian &ecan be destroyed"));
		hologram.appendTextLine(StringUtil.color("&eby &5" + required + " &eTNT explosions"));
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if(item != null && item.equals(event.getItem())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onItemDespawn(ItemDespawnEvent event) {
		if(item != null && item.equals(event.getEntity()) && item.getFireTicks() < 9000) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onServerRestart(ServerRestartEvent event) {
		if(item != null) {
			item.setFireTicks(9001);
			item.remove();
		}
	}
	
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event) {
		if(event.getEntityType() != null && event.getEntityType() == EntityType.PRIMED_TNT) {
			Block block = event.getLocation().getBlock();
			for(int x = -7; x <= 7; ++x) {
				for(int y = -7; y <= 7; ++y) {
					for(int z = -7; z <= 7; ++z) {
						Block relative = block.getRelative(x, y, z);
						if(relative.getType() == Material.OBSIDIAN) {
							int counter = 0;
							if(counters.containsKey(relative)) {
								counter = counters.get(relative);
							}
							if(++counter >= required) {
								counters.remove(relative);
								relative.setType(Material.AIR);
							} else {
								counters.put(relative, counter);
							}
						}
					}
				}
			}
		}
	}
}
