package promcgames.gameapi.games.factions.spawn;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import promcgames.customevents.timed.TenSecondTaskEvent;
import promcgames.server.util.EventUtil;

public class Spawners implements Listener {
	public class Region {
		private Location min = null;
		private Location max = null;
		private List<LivingEntity> entities = null;
		
		public Region(int x1, int y1, int z1, int x2, int y2, int z2) {
			min = new Location(world, x1, y1, z1);
			max = new Location(world, x2, y2, z2);
			entities = new ArrayList<LivingEntity>();
			if(regions == null) {
				regions = new ArrayList<Region>();
			}
			regions.add(this);
		}
		
		public Location getMin() {
			return this.min;
		}
		
		public Location getMax() {
			return this.max;
		}
		
		public void addEntity(LivingEntity livingEntity) {
			entities.add(livingEntity);
		}
		
		public void removeEntity(LivingEntity livingEntity) {
			entities.remove(livingEntity);
		}
		
		public boolean containsEntity(LivingEntity livingEntity) {
			return entities.contains(livingEntity);
		}
		
		public int getAmountOfEntities() {
			return entities.size();
		}
		
		public Location getCenter() {
			int x1 = min.getBlockX();
			int z1 = min.getBlockZ();
			int x2 = max.getBlockX();
			int z2 = max.getBlockZ();
			int x = (x2 - x1) / 2;
			int z = (z2 - z1) / 2;
			return new Location(world, x2 - x, min.getBlockY(), z2 - z);
		}
	}
	
	private List<Region> regions = null;
	private World world = null;
	
	public Spawners() {
		world = Bukkit.getWorlds().get(0);
		new Region(-303, 63, 275, -295, 65, 283);
		new Region(-300, 63, 262, -292, 65, 270);
		new Region(-287, 63, 259, -279, 65, 267);
		EventUtil.register(this);
	}
	
	@EventHandler
	public void onTenSecondTask(TenSecondTaskEvent event) {
		for(Region region : regions) {
			if(region.getAmountOfEntities() < 5) {
				LivingEntity livingEntity = (LivingEntity) world.spawnEntity(region.getCenter(), EntityType.PIG);
				region.addEntity(livingEntity);
			}
		}
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event) {
		Entity entity = event.getEntity();
		if(entity instanceof LivingEntity && !(entity instanceof Player) && !(entity instanceof Monster)) {
			LivingEntity livingEntity = (LivingEntity) entity;
			for(Region region : regions) {
				if(region.containsEntity(livingEntity)) {
					region.removeEntity(livingEntity);
					livingEntity.remove();
					event.getDrops().clear();
					event.getDrops().add(new ItemStack(Material.GRILLED_PORK));
				}
			}
		}
	}
}
