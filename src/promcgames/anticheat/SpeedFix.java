package promcgames.anticheat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.potion.PotionEffectType;

import promcgames.customevents.player.PlayerLeaveEvent;
import promcgames.customevents.timed.FiveSecondTaskEvent;
import promcgames.customevents.timed.OneSecondTaskEvent;
import promcgames.server.PerformanceHandler;
import promcgames.server.util.EventUtil;

public class SpeedFix extends AntiGamingChair implements Listener {
	private Map<String, Location> lastLocations = null;
	private Map<String, Integer> counters = null;
	private Map<String, Integer> disabledCounters = null;
	private static Map<String, Double> speeds = null;
	
	public SpeedFix() {
		super("Speed");
		lastLocations = new HashMap<String, Location>();
		counters = new HashMap<String, Integer>();
		disabledCounters = new HashMap<String, Integer>();
		speeds = new HashMap<String, Double>();
		EventUtil.register(this);
	}
	
	private void disable(Player player, int seconds) {
		disabledCounters.put(player.getName(), seconds);
	}
	
	public static double getSpeed(Player player) {
		return speeds == null || !speeds.containsKey(player.getName()) ? 0.0d : speeds.get(player.getName());
	}
	
	@EventHandler
	public void onOneSecondTask(OneSecondTaskEvent event) {
		if(isEnabled()) {
			Iterator<String> iterator = disabledCounters.keySet().iterator();
			while(iterator.hasNext()) {
				String name = iterator.next();
				int counter = disabledCounters.get(name);
				if(--counter <= 0) {
					iterator.remove();
				} else {
					disabledCounters.put(name, counter);
				}
			}
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(PerformanceHandler.getPing(player) < getMaxPing()) {
					Location from = player.getLocation();
					if(lastLocations.containsKey(player.getName())) {
						from = lastLocations.get(player.getName());
					}
					Location to = player.getLocation();
					double x1 = to.getX();
					double y1 = to.getY();
					double z1 = to.getZ();
					double x2 = from.getX();
					double y2 = from.getY();
					double z2 = from.getZ();
					double distance = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));
					speeds.put(player.getName(), distance);
					if(!player.getAllowFlight() && !player.isFlying() && player.getVehicle() == null && to.getWorld().getName().equals(from.getWorld().getName()) && !player.hasPotionEffect(PotionEffectType.SPEED)) {
						if(to.getY() >= from.getY() && !disabledCounters.containsKey(player.getName()) && notIgnored(player) && (to.getX() != from.getX() || to.getY() != from.getY() || to.getZ() != from.getZ())) {
							if(distance >= 5.0d && player.isSneaking()) {
								player.setSneaking(false);
							}
							boolean somethingAbove = player.getLocation().add(0, 2, 0).getBlock().getType() != Material.AIR;
							if(!somethingAbove && distance >= 8.5) {
								int counter = 0;
								if(counters.containsKey(player.getName())) {
									counter = counters.get(player.getName());
								}
								if(++counter >= 3) {
									ban(player);
								} else {
									counters.put(player.getName(), counter);
								}
							}
						}
					}
					lastLocations.put(player.getName(), to);
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(isEnabled() && event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			disable(player, 5);
		}
	}
	
	@EventHandler
	public void onPlayerVelocity(PlayerVelocityEvent event) {
		if(isEnabled()) {
			disable(event.getPlayer(), 5);
		}
	}
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if(isEnabled()) {
			lastLocations.remove(event.getPlayer().getName());
			counters.remove(event.getPlayer().getName());
			disable(event.getPlayer(), 5);
		}
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if(isEnabled()) {
			Location to = event.getTo();
			String below = to.getBlock().getRelative(0, -1, 0).getType().toString();
			double toY = to.getY();
			if(below.contains("ICE") || below.contains("STAIR") || below.contains("SLAB") || (!(toY == Math.floor(toY) && !Double.isInfinite(toY)) && toY - (int) toY == .5)) {
				lastLocations.remove(event.getPlayer().getName());
				counters.remove(event.getPlayer().getName());
			}
		}
	}
	
	@EventHandler
	public void onFiveSecondTask(FiveSecondTaskEvent event) {
		if(isEnabled()) {
			counters.clear();
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerLeaveEvent event) {
		speeds.remove(event.getPlayer().getName());
	}
}
