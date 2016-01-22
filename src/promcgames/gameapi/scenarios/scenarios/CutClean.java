package promcgames.gameapi.scenarios.scenarios;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import promcgames.gameapi.scenarios.Scenario;
import promcgames.server.util.EffectUtil;
import promcgames.server.util.ItemCreator;

@SuppressWarnings("deprecation")
public class CutClean extends Scenario {
	private static CutClean instance = null;
	
	public CutClean() {
		super("CutClean", Material.IRON_INGOT);
		instance = this;
		setInfo("Ores auto smelt and food auto cooks. Breaking the base of a tree breaks the whole tree.");
	}
	
	public static CutClean getInstance() {
		if(instance == null) {
			new CutClean();
		}
		return instance;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDeath(EntityDeathEvent event) {
		ItemStack mutton = new ItemCreator(Material.PORK).setName("&fRaw Mutton").getItemStack();
		if(event.getEntityType() == EntityType.SHEEP && event.getDrops().contains(mutton)) {
			event.getDrops().remove(mutton);
			event.getDrops().add(new ItemCreator(Material.GRILLED_PORK).setName("&fCooked Mutton").getItemStack());
		}
	}
	
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		Material material = event.getEntity().getItemStack().getType();
		Entity entity = event.getEntity();
		World world = entity.getWorld();
		if(material == Material.GOLD_ORE) {
			world.dropItem(event.getLocation(), new ItemStack(Material.GOLD_INGOT));
			ExperienceOrb exp = (ExperienceOrb) world.spawnEntity(event.getLocation(), EntityType.EXPERIENCE_ORB);
			exp.setExperience(1);
			entity.remove();
		} else if(material == Material.IRON_ORE) {
			world.dropItem(event.getLocation(), new ItemStack(Material.IRON_INGOT));
			ExperienceOrb exp = (ExperienceOrb) world.spawnEntity(event.getLocation(), EntityType.EXPERIENCE_ORB);
			exp.setExperience(1);
			entity.remove();
		} else if(material == Material.RAW_BEEF) {
			world.dropItem(event.getLocation(), new ItemStack(Material.COOKED_BEEF));
			entity.remove();
		} else if(material == Material.RAW_CHICKEN) {
			world.dropItem(event.getLocation(), new ItemStack(Material.COOKED_CHICKEN));
			entity.remove();
		} else if(material == Material.RAW_FISH) {
			world.dropItem(event.getLocation(), new ItemStack(Material.COOKED_FISH));
			entity.remove();
		} else if(material == Material.POTATO) {
			world.dropItem(event.getLocation(), new ItemStack(Material.BAKED_POTATO));
			entity.remove();
		} else if(material == Material.PORK) {
			Item item = (Item) entity;
			ItemStack itemStack = item.getItemStack();
			ItemMeta meta = itemStack.getItemMeta();
			if(meta != null && meta.getDisplayName() != null && meta.getDisplayName().contains("Mutton")) {
				world.dropItem(event.getLocation(), new ItemCreator(Material.GRILLED_PORK).setName("&fCooked Mutton").getItemStack());
			} else {
				world.dropItem(event.getLocation(), new ItemStack(Material.GRILLED_PORK));
			}
			entity.remove();
		} else if(material == Material.GRAVEL && new Random().nextBoolean()) {
			world.dropItem(event.getLocation(), new ItemStack(Material.FLINT));
			entity.remove();
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if(!event.isCancelled()) {
			Block block = event.getBlock();
			Material below = block.getRelative(0, -1, 0).getType();
			if(below == Material.DIRT || below == Material.SAND) {
				Location location = block.getLocation().clone();
				int id = block.getTypeId();
				while(id == 162 || id == 17) {
					EffectUtil.displayParticles(block.getType(), block.getLocation());
					for(ItemStack itemStack : block.getDrops()) {
						block.getWorld().dropItem(location, itemStack);
					}
					block.setType(Material.AIR);
					block = block.getRelative(0, 1, 0);
					id = block.getTypeId();
				}
			}
		}
	}
}
