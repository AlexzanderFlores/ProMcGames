package promcgames.server.servers.hub.items.cosmetic;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.timed.FifteenTickTaskEvent;
import promcgames.player.Particles;
import promcgames.player.Particles.ParticleTypes;
import promcgames.server.DB;
import promcgames.server.ProPlugin;
import promcgames.server.servers.hub.items.cosmetic.elite.SpiralParticles;
import promcgames.server.servers.hub.items.cosmetic.pro.ArmorSelectorItem;
import promcgames.server.servers.hub.items.cosmetic.pro.ArmorSelectorItem.ArmorTypes;
import promcgames.server.servers.hub.items.cosmetic.pro.pets.PetSpawningHandler;
import promcgames.server.util.EventUtil;

@SuppressWarnings("deprecation")
public class PerkLoader implements Listener {
	public static enum Perk {PET, ARMOR, PARTICLES, HATS, SPIRAL_PARTICLES}
	private static List<String> queue = null;
	
	public PerkLoader() {
		queue = new ArrayList<String>();
		EventUtil.register(this);
	}
	
	public static void addPerkToQueue(Player player, Perk perk) {
		String name = player.getName();
		queue.add(name + "-" + perk.toString());
	}
	
	@EventHandler
	public void onFifteenTickTask(FifteenTickTaskEvent event) {
		if(!queue.isEmpty()) {
			String item = queue.get(0);
			String [] itemDetails = item.split("-");
			Player player = ProPlugin.getPlayer(itemDetails[0]);
			if(player != null) {
				try {
					Perk perk = Perk.valueOf(itemDetails[1]);
					if(perk == Perk.PET) {
						String type = DB.HUB_PETS.getString("uuid", player.getUniqueId().toString(), "pet_type");
						if(type != null) {
							PetSpawningHandler.spawn(player, EntityType.valueOf(type));
						}
					} else if(perk == Perk.ARMOR) {
						String type = DB.HUB_ARMOR.getString("uuid", player.getUniqueId().toString(), "armor_type");
						if(type != null) {
							ArmorTypes armorTypes = ArmorTypes.valueOf(type);
							ArmorSelectorItem.applyArmor(player, armorTypes);
						}
					} else if(perk == Perk.PARTICLES) {
						String type = DB.HUB_PARTICLES.getString("uuid", player.getUniqueId().toString(), "particle_type");
						if(type != null) {
							Particles.setType(player, ParticleTypes.valueOf(type));
						}
					} else if(perk == Perk.HATS) {
						String hat = DB.HUB_HATS.getString("uuid", player.getUniqueId().toString(), "hat");
						if(hat != null) {
							int id = Integer.valueOf(hat.split(":")[0]);
							byte data = Byte.valueOf(hat.split(":")[1]);
							player.getInventory().setHelmet(new ItemStack(id, 1, data));
						}
					} else if(perk == Perk.SPIRAL_PARTICLES) {
						String type = DB.HUB_SPIRAL_PARTICLES.getString("uuid", player.getUniqueId().toString(), "particle_type");
						if(type != null) {
							SpiralParticles.setType(player, ParticleTypes.valueOf(type));
						}
					}
				} catch(IllegalArgumentException e) {
					
				}
			}
			queue.remove(0);
		}
	}
}
