package promcgames.gameapi.games.arcade.games.dragonraces;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_7_R4.EntityEnderDragon;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEnderDragon;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import promcgames.customevents.timed.OneTickTaskEvent;
import promcgames.player.Disguise;
import promcgames.player.MessageHandler;
import promcgames.server.ProPlugin;
import promcgames.server.tasks.DelayedTask;
import promcgames.server.util.EventUtil;
import promcgames.server.util.VectorUtil;

@SuppressWarnings("deprecation")
public class DragonHandler implements Listener {
	private static List<EnderDragon> movingAwayFromMountain = null;
	private static List<String> messageDelay = null;
	private static World world = null;
	
	public DragonHandler(World world) {
		movingAwayFromMountain = new ArrayList<EnderDragon>();
		messageDelay = new ArrayList<String>();
		DragonHandler.world = world;
		for(Player player : ProPlugin.getPlayers()) {
			EnderDragon dragon = (EnderDragon) world.spawnEntity(player.getLocation(), EntityType.ENDER_DRAGON);
			dragon.setPassenger(player);
		}
		EventUtil.register(this);
	}
	
	public static void disable() {
		movingAwayFromMountain = null;
		messageDelay = null;
		world = null;
	}
	
	@EventHandler
	public void onOneTickTask(OneTickTaskEvent event) {
		for(Entity entity : world.getEntities()) {
			if(entity instanceof EnderDragon && entity.getPassenger() != null && entity.getPassenger() instanceof Player) {
				EnderDragon dragon = (EnderDragon) entity;
				final Player player = (Player) dragon.getPassenger();
				Vector direction = movingAwayFromMountain.contains(dragon) ? dragon.getVelocity() : player.getLocation().getDirection();
				if(dragon.getLocation().getBlockY() < 63) {
					direction.add(new Vector(0, 2.5d, 0));
				} else if(dragon.getLocation().getBlockY() > 90) {
					if(!messageDelay.contains(Disguise.getName(player))) {
						messageDelay.add(Disguise.getName(player));
						new DelayedTask(new Runnable() {
							@Override
							public void run() {
								messageDelay.remove(Disguise.getName(player));
							}
						}, 30);
						MessageHandler.sendMessage(player, "&eYou have reached the height limit");
					}
					direction.add(new Vector(0, -2.5d, 0));
				}
				if(!movingAwayFromMountain.contains(dragon)) {
					if(player.hasPotionEffect(PotionEffectType.SPEED)) {
						dragon.setVelocity(direction.multiply(4.0d));
					} else if(player.hasPotionEffect(PotionEffectType.SLOW)) {
						dragon.setVelocity(direction.multiply(0.40d));
					} else {
						dragon.setVelocity(direction.multiply(1.5d));
					}
					dragon.setVelocity(player.getLocation().getDirection());
					CraftEnderDragon craftEnderDragon = (CraftEnderDragon) dragon;
					EntityEnderDragon entityEnderDragon = craftEnderDragon.getHandle();
					Location location = dragon.getLocation();
					entityEnderDragon.setPositionRotation(location.getX(), location.getY(), location.getZ(), player.getLocation().getYaw() + 180, player.getLocation().getPitch() * -1);
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Fireball) {
			Player player = null;
			if(event.getEntity() instanceof Player) {
				player = (Player) event.getEntity();
			} else if(event.getEntity() instanceof EnderDragon) {
				EnderDragon dragon = (EnderDragon) event.getEntity();
				if(dragon.getPassenger() != null && dragon.getPassenger() instanceof Player) {
					player = (Player) dragon.getPassenger();
				}
			}
			if(player != null) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 5, 0));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onEntityExplode(EntityExplodeEvent event) {
		if(event.getEntity() instanceof EnderDragon && event.blockList().size() > 0 && event.getEntity().getPassenger() != null && event.getEntity().getPassenger() instanceof Player) {
			final EnderDragon dragon = (EnderDragon) event.getEntity();
			int numberOfInvisBlocks = 0;
			for(Block block: event.blockList()) {
				if(block.getTypeId() == 36 && block.getData() == (byte) 6) {
					++numberOfInvisBlocks;
				} else if(block.getType() == Material.TNT) {
					block.setType(Material.AIR);
					Player player = (Player) dragon.getPassenger();
					player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 5, 0));
				}
			}
			if(numberOfInvisBlocks < event.blockList().size()) {
				movingAwayFromMountain.add(dragon);
				dragon.setVelocity(VectorUtil.getDirectionVector(dragon.getLocation(), event.blockList().get(0).getLocation(), -0.25d));
				new DelayedTask(new Runnable() {
					@Override
					public void run() {
						movingAwayFromMountain.remove(dragon);
					}
				}, 2);
			}
		}
	}
}
